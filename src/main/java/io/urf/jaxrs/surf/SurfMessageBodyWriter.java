/*
 * Copyright © 2017 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.urf.jaxrs.surf;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.*;

import javax.annotation.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;

import com.globalmentor.itu.TelephoneNumber;

import io.ploop.introspection.*;

import io.urf.surf.parser.SurfObject;
import io.urf.surf.serializer.SurfSerializer;

import static java.util.Objects.*;

/**
 * Class created to provide support for a SURF {@link MediaType} in order to be used with JAX-RS.
 * 
 * @author Magno N A Cruz
 *
 * @param <T> The type of object to be serialized by this class.
 */
@Provider
@Produces("text/surf")
public class SurfMessageBodyWriter<T> implements MessageBodyWriter<T> {

	private final static String ARRAY_LIST_CLASS_NAME = "java.util.ArrayList";
	private final static String BIG_DECIMAL_CLASS_NAME = "java.math.BigDecimal";
	private final static String BIG_INTEGER_CLASS_NAME = "java.math.BigInteger";
	private final static String BOOLEAN_CLASS_NAME = "java.lang.Boolean";
	private final static String BYTE_CLASS_NAME = "java.lang.Byte";
	private final static String BYTE_ARRAY_CLASS_NAME = "[B";
	private final static String CHARACTER_CLASS_NAME = "java.lang.Character";
	private final static String CODE_POINT_CHARACTER_CLASS_NAME = "com.globalmentor.java.CodePointCharacter";
	private final static String DATE_CLASS_NAME = "java.util.Date";
	private final static String DOUBLE_CLASS_NAME = "java.lang.Double";
	private final static String EMAIL_ADDRESS_CLASS_NAME = "com.globalmentor.net.EmailAddress";
	private final static String FLOAT_CLASS_NAME = "java.lang.Float";
	private final static String HASH_MAP_CLASS_NAME = "java.util.HashMap";
	private final static String HASH_SET_CLASS_NAME = "java.util.HashSet";
	private final static String INSTANT_CLASS_NAME = "java.time.Instant";
	private final static String INTEGER_CLASS_NAME = "java.lang.Integer";
	private final static String LINKED_HASH_MAP_CLASS_NAME = "java.util.LinkedHashMap";
	private final static String LINKED_HASH_SET_CLASS_NAME = "java.util.LinkedHashSet";
	private final static String LINKED_LIST_CLASS_NAME = "java.util.LinkedList";
	private final static String LOCAL_DATE_CLASS_NAME = "java.time.LocalDate";
	private final static String LOCAL_DATE_TIME_CLASS_NAME = "java.time.LocalDateTime";
	private final static String LOCAL_TIME_CLASS_NAME = "java.time.LocalTime";
	private final static String LONG_CLASS_NAME = "java.lang.Long";
	private final static String MONTH_DAY_CLASS_NAME = "java.time.MonthDay";
	private final static String OFFSET_DATE_TIME_CLASS_NAME = "java.time.OffsetDateTime";
	private final static String OFFSET_TIME_CLASS_NAME = "java.time.OffsetTime";
	private final static String PATTERN_CLASS_NAME = "java.util.regex.Pattern";
	private final static String SHORT_CLASS_NAME = "java.lang.Short";
	private final static String STRING_CLASS_NAME = "java.lang.String";
	private final static String STRING_BUILDER_CLASS_NAME = "java.lang.StringBuilder";
	private final static String TELEPHONE_NUMBER_CLASS_NAME = "com.globalmentor.itu.TelephoneNumber";
	private final static String TREE_MAP_CLASS_NAME = "java.util.TreeMap";
	private final static String TREE_SET_CLASS_NAME = "java.util.TreeSet";
	private final static String URI_CLASS_NAME = "java.net.URI";
	private final static String URL_CLASS_NAME = "java.net.URL";
	private final static String UUID_CLASS_NAME = "java.util.UUID";
	private final static String YEAR__CLASS_NAME = "java.time.Year";
	private final static String YEAR_MONTH_CLASS_NAME = "java.time.YearMonth";
	private final static String ZONED_DATE_TIME_CLASS_NAME = "java.time.ZonedDateTime";

	@Deprecated
	@Override
	public long getSize(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	@Override
	public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
		return type != null;
	}

	@Override
	public void writeTo(final T objectToWrite, final Class<?> clazz, final Type type, final Annotation[] annotations, final MediaType mediaType,
			final MultivaluedMap<String, Object> valueMap, final OutputStream out) throws IOException, WebApplicationException {

		if(objectToWrite == null) {
			return; //TODO see if it's better to just return or if we must throw an exception.
		}

		final SurfSerializer serializer = new SurfSerializer();
		serializer.setFormatted(true);

		try {

			if(out instanceof Appendable) {
				serializer.serialize((Appendable)out, transformObject(objectToWrite)); //this is needed because when an output stream like `System.out` is provided, SURF doesn't know what method to call.
			} else {
				serializer.serialize(out, transformObject(objectToWrite));
			}

		} catch(IllegalAccessException | InvocationTargetException e) {
			throw new IllegalStateException("The object provided could not be converted into a SURF Object.", e);
		}

	}

	/**
	 * Helper method used to convert a general object provided in an object supported by {@link SurfSerializer}.
	 * 
	 * @param obj The object to be converted.
	 * @return The object converted into
	 * @throws InvocationTargetException if some underlying method of a class being inspected throws an exception.
	 * @throws IllegalAccessException If the object being inspected is enforcing Java language access control some underlying method is inaccessible.
	 */
	private <P> Object transformObject(@Nonnull final P obj) throws IllegalAccessException, InvocationTargetException {

		if(obj == null) { //if the object is null we simply return it. The root object must not be null and it might be verified before the first call for this method.
			return null;
		}

		if(obj instanceof List) { //if the provided method is a list, we just convert the values that needs to be converted, i.e., the values that are not natively supported by SURF.
			final List<?> providedList = (List<?>)obj;
			final List<Object> newList = new LinkedList<>();

			for(final Object value : providedList) {
				newList.add(transformObject(value));
			}

			return newList;
		}

		if(obj instanceof Set) { //if the provided method is a set, we just convert the values that needs to be converted, i.e., the values that are not natively supported by SURF.
			final Set<?> providedSet = (Set<?>)obj;
			final Set<Object> newSet = new HashSet<>();

			for(final Object value : providedSet) {
				newSet.add(transformObject(value));
			}

			return newSet;
		}

		if(obj instanceof Map) { //if the provided method is a map, we just convert the children name-value pairs that needs to be converted, i.e., the values that are not natively supported by SURF.
			final Map<?, ?> providedMap = (Map<?, ?>)obj;
			final Map<Object, Object> newMap = new HashMap<>();

			for(final Object key : providedMap.keySet()) {
				newMap.put(key, transformObject(providedMap.get(key)));
			}

			return newMap;
		}

		if(isSupported(obj)) { //if SURF can handle with this type of object and it's not one of the data structure listed above, we don't need to convert it to anything else.
			return obj;
		}

		@SuppressWarnings("unchecked")
		final Introspection<P> introspection = (Introspection<P>)Introspection.of(obj.getClass());
		final Collection<Property<P, ?>> properties = introspection.getProperties();

		final SurfObject newSurfObject = new SurfObject(introspection.getObjectType().getErasedType().getSimpleName());

		for(final Property<P, ?> property : properties) {

			if(property.getValue(obj) != null) {
				newSurfObject.setPropertyValue(property.getName(), transformObject(property.getValue(obj)));
			}

		}

		final SurfSerializer serializer = new SurfSerializer();
		serializer.setFormatted(true);

		return newSurfObject;
	}

	/**
	 * Returns whether a {@link SurfObject} offers support for the provided object as a property.
	 * <p>
	 * This method must be updated <strong>every time that SURF gets support to a new type of property</strong>.
	 * </p>
	 * 
	 * @param obj The object to be verified.
	 * @return {@code true} whether the object is supported, {@code false} if not.
	 */
	private boolean isSupported(@Nonnull final Object obj) {
		requireNonNull(obj, "The object provided must not be <null>.");

		switch(obj.getClass().getName()) {
			case BYTE_ARRAY_CLASS_NAME:
			case BOOLEAN_CLASS_NAME:
			case CHARACTER_CLASS_NAME:
			case CODE_POINT_CHARACTER_CLASS_NAME:
			case EMAIL_ADDRESS_CLASS_NAME:
			case URI_CLASS_NAME:
			case URL_CLASS_NAME:
			case BIG_DECIMAL_CLASS_NAME:
			case BIG_INTEGER_CLASS_NAME:
			case BYTE_CLASS_NAME:
			case DOUBLE_CLASS_NAME:
			case FLOAT_CLASS_NAME:
			case INTEGER_CLASS_NAME:
			case LONG_CLASS_NAME:
			case SHORT_CLASS_NAME:
			case PATTERN_CLASS_NAME:
			case STRING_CLASS_NAME:
			case STRING_BUILDER_CLASS_NAME:
			case TELEPHONE_NUMBER_CLASS_NAME:
			case DATE_CLASS_NAME:
			case INSTANT_CLASS_NAME:
			case MONTH_DAY_CLASS_NAME:
			case LOCAL_DATE_CLASS_NAME:
			case LOCAL_DATE_TIME_CLASS_NAME:
			case LOCAL_TIME_CLASS_NAME:
			case OFFSET_DATE_TIME_CLASS_NAME:
			case OFFSET_TIME_CLASS_NAME:
			case YEAR__CLASS_NAME:
			case YEAR_MONTH_CLASS_NAME:
			case ZONED_DATE_TIME_CLASS_NAME:
			case UUID_CLASS_NAME:
			case ARRAY_LIST_CLASS_NAME:
			case LINKED_LIST_CLASS_NAME:
			case HASH_MAP_CLASS_NAME:
			case LINKED_HASH_MAP_CLASS_NAME:
			case TREE_MAP_CLASS_NAME:
			case HASH_SET_CLASS_NAME:
			case LINKED_HASH_SET_CLASS_NAME:
			case TREE_SET_CLASS_NAME:
				return true;
			default:
				if(obj instanceof SurfObject) { //objects
					return true;
				} else if(obj instanceof List) { //list
					return true;
				} else if(obj instanceof Map) { //map
					return true;
				} else if(obj instanceof Set) { //set
					return true;
				} else if(obj instanceof ByteBuffer) { //binary
					return true;
				} else if(obj instanceof Number) { //number
					return true;
				} else if(obj instanceof CharSequence) { //string
					return true;
				} else if(obj instanceof TelephoneNumber) { //telephone number
					return true;
				} else if(obj instanceof Date) { //temporal
					return true;
				} else {
					return false;
				}
		}

	}

}
