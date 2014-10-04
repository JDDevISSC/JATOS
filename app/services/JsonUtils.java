package services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.TimeZone;

import models.results.ComponentResult;
import models.results.StudyResult;
import models.workers.Worker;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import play.Logger;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.publix.Publix;
import exceptions.ResultException;

public class JsonUtils {

	private static final String CLASS_NAME = Publix.class.getSimpleName();
	private static final ObjectMapper OBJECTMAPPER = new ObjectMapper()
			.setTimeZone(TimeZone.getDefault());

	/**
	 * Helper class for selectively marshaling an Object to JSON. Only fields of
	 * that Object that are annotated with this class will be serialised. The
	 * intended use is in the public API.
	 */
	public static class JsonForPublix {
	}

	/**
	 * Helper class for selectively marshaling an Object to JSON. Only fields of
	 * that Object that are annotated with this class will be serialised. The
	 * intended use is in the MechArg.
	 */
	public static class JsonForMA extends JsonForPublix {
	}

	/**
	 * Helper class for selectively marshaling an Object to JSON. Only fields of
	 * that Object that are annotated with this class will be serialised.
	 * Intended use: import/export between different instances of the MechArg.
	 */
	public static class JsonForIO {
	}

	/**
	 * Turns a JSON string into a 'pretty' formatted JSON string suitable for
	 * presentation in a UI. The JSON itself (semantics) aren't changed. If the
	 * JSON string isn't valid it returns null.
	 */
	public static String makePretty(String jsonData) {
		String jsonDataPretty = null;
		try {
			Object json = OBJECTMAPPER.readValue(jsonData, Object.class);
			jsonDataPretty = OBJECTMAPPER.writerWithDefaultPrettyPrinter()
					.writeValueAsString(json);
		} catch (Exception e) {
			Logger.info(CLASS_NAME + ".makePretty: ", e);
		}
		return jsonDataPretty;
	}

	/**
	 * Formats a JSON string into a standardised form suitable for storing into
	 * a DB. If the JSON string isn't valid it returns null.
	 */
	public static String asStringForDB(String jsonDataStr) {
		try {
			String jsonData = OBJECTMAPPER.readTree(jsonDataStr).toString();
			return jsonData;
		} catch (Exception e) {
			Logger.info(CLASS_NAME + ".asStringForDB: ", e);
		}
		return null;
	}

	/**
	 * Checks whether the given string is a valid JSON string.
	 */
	public static boolean isValidJSON(final String jsonDataStr) {
		boolean valid = false;
		try {
			// Parse the string. If an exception occurs return false and true
			// otherwise.
			final JsonParser parser = OBJECTMAPPER.getFactory().createParser(
					jsonDataStr);
			while (parser.nextToken() != null) {
			}
			valid = true;
		} catch (Exception e) {
			Logger.info(CLASS_NAME + ".isValidJSON: ", e);
			valid = false;
		}
		return valid;
	}

	/**
	 * Marshalling an Object into an JSON string. It only considers fields that
	 * are annotated with 'JsonForPublix'.
	 * 
	 * @throws JsonProcessingException
	 */
	public static String asJsonForPublix(Object obj)
			throws JsonProcessingException {
		ObjectWriter objectWriter = OBJECTMAPPER
				.writerWithView(JsonForPublix.class);
		String componentAsJson = objectWriter.writeValueAsString(obj);
		return componentAsJson;
	}

	/**
	 * Marshalling an Object into an JSON string. It considers the default
	 * timezone.
	 * 
	 * @throws JsonProcessingException
	 */
	public static String asJsonForMA(Object obj) throws JsonProcessingException {
		ObjectWriter objectWriter = OBJECTMAPPER
				.writerWithView(JsonForMA.class);
		String resultAsJson = objectWriter.writeValueAsString(obj);
		return resultAsJson;
	}

	/**
	 * Marshalling a ComponentResult into an JSON string. It considers the
	 * default timezone.
	 * 
	 * @throws JsonProcessingException
	 */
	public static String componentResultAsJsonForMA(
			ComponentResult componentResult) throws JsonProcessingException {
		ObjectNode componentResultNode = OBJECTMAPPER
				.valueToTree(componentResult);

		// Add studyId and componentId
		componentResultNode.put("studyId", componentResult.getComponent()
				.getStudy().getId());
		componentResultNode.put("componentId", componentResult.getComponent()
				.getId());

		// Add worker
		StudyResult studyResult = componentResult.getStudyResult();
		Worker worker = initializeAndUnproxy(studyResult.getWorker());
		ObjectNode workerNode = OBJECTMAPPER.valueToTree(worker);
		componentResultNode.with("worker").putAll(workerNode);

		// Write as string
		String resultAsJson = OBJECTMAPPER
				.writeValueAsString(componentResultNode);

		// Add componentResult's data to the end
		resultAsJson = resultAsJson.substring(0, resultAsJson.length() - 1);
		resultAsJson = resultAsJson + ",\"data\":" + componentResult.getData()
				+ "}";

		return resultAsJson;
	}

	@SuppressWarnings("unchecked")
	public static <T> T initializeAndUnproxy(T obj) {
		Hibernate.initialize(obj);
		if (obj instanceof HibernateProxy) {
			obj = (T) ((HibernateProxy) obj).getHibernateLazyInitializer()
					.getImplementation();
		}
		return obj;
	}

	public static String asJsonForIO(Object obj) throws JsonProcessingException {
		ObjectWriter objectWriter = OBJECTMAPPER
				.writerWithView(JsonForIO.class);
		String objectAsJson = objectWriter.writeValueAsString(obj);
		return objectAsJson;
	}

	public static <T> T unmarshalling(String jsonStr, Class<T> model)
			throws JsonParseException, JsonMappingException, IOException {
		T object = OBJECTMAPPER.readValue(jsonStr, model);
		return object;
	}

	public static <T> T rippingObjectFromJsonUploadRequest(MultipartFormData mfd,
			Class<T> model) throws ResultException {
		FilePart filePart = mfd.getFile("component");
		String errorMsg = null;
		T object = null;
		if (filePart != null) {
			File file = filePart.getFile();
			String jsonStr = null;
			try {
				jsonStr = readFile(file);
			} catch (IOException e1) {
				errorMsg = ErrorMessages.couldntReadFile();
			}
			try {
				object = JsonUtils.unmarshalling(jsonStr, model);
			} catch (IOException e) {
				errorMsg = ErrorMessages.couldntReadJson();
			}
		} else {
			errorMsg = ErrorMessages.fileMissing();
		}

		if (object != null) {
			return object;
		} else {
			throw new ResultException(errorMsg);
		}
	}

	public static String readFile(File file) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			return sb.toString();
		}
	}

}
