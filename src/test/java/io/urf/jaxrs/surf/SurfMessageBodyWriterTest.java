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

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.*;

import org.junit.*;

import io.urf.surf.parser.SurfObject;
import io.urf.surf.serializer.SurfSerializer;

/**
 * Class created to test the method of {@link SurfMessageBodyWriter}.
 * 
 * @author Magno N A Cruz
 */
public class SurfMessageBodyWriterTest {

	/**
	 * Tests whether the method
	 * {@link SurfMessageBodyWriter#writeTo(Object, Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)}
	 * is working correctly and it's transforming the objects into instances of {#link {@link SurfObject SurfObjects}.
	 * 
	 * @throws IOException If an I/O error occurs.
	 */
	@Test
	public void testWriteToWithJavaBeanWithComplexProperties() throws IOException {

		final SurfSerializer serializer = new SurfSerializer();
		serializer.setFormatted(true);

		final SurfMessageBodyWriter<Object> surfMessageBodyWriter = new SurfMessageBodyWriter<>();

		try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

			//test with a <null> source object.
			surfMessageBodyWriter.writeTo(null, null, null, null, null, null, baos);

			assertThat(baos.toString(StandardCharsets.UTF_8.name()), equalTo(""));

			baos.reset();

			//test with an empty SimpleFooBarBean source object.
			final SimpleFooBarBean emptyFooBar = new SimpleFooBarBean();

			surfMessageBodyWriter.writeTo(emptyFooBar, null, null, null, null, null, baos);

			assertThat(baos.toString(StandardCharsets.UTF_8.name()), equalTo(serializer.serialize(new SurfObject("SimpleFooBarBean"))));

			baos.reset();

			//test with a SimpleFooBarBean source object.
			final SimpleFooBarBean simpleFooBarBean = new SimpleFooBarBean();
			simpleFooBarBean.setFoo("foo");
			simpleFooBarBean.setBar("bar");

			surfMessageBodyWriter.writeTo(simpleFooBarBean, null, null, null, null, null, baos);

			final SurfObject simpleFooBarSurfObject = new SurfObject("SimpleFooBarBean");
			simpleFooBarSurfObject.setPropertyValue("foo", "foo");
			simpleFooBarSurfObject.setPropertyValue("bar", "bar");

			assertThat(baos.toString(StandardCharsets.UTF_8.name()), equalTo(serializer.serialize(simpleFooBarSurfObject)));

			baos.reset();

			//test with a SimpleFooBarBean source object without value for the attribute bar.
			final SimpleFooBarBean fooWithoutBar = new SimpleFooBarBean();
			fooWithoutBar.setFoo("foo");

			surfMessageBodyWriter.writeTo(fooWithoutBar, null, null, null, null, null, baos);

			final SurfObject simpleFooWithoutBarSurfObject = new SurfObject("SimpleFooBarBean");
			simpleFooWithoutBarSurfObject.setPropertyValue("foo", "foo");

			assertThat(baos.toString(StandardCharsets.UTF_8.name()), equalTo(serializer.serialize(simpleFooWithoutBarSurfObject)));

			baos.reset();

			//test with a ComplexFooBarBean source object that has a SimpleFooBarBean as type of attribute bar.
			final ComplexFooBarBean complexFooBarBean = new ComplexFooBarBean();
			complexFooBarBean.setFoo("foo");

			complexFooBarBean.setBar(simpleFooBarBean);

			surfMessageBodyWriter.writeTo(complexFooBarBean, null, null, null, null, null, baos);

			final SurfObject complexFooBarSurfObject = new SurfObject("ComplexFooBarBean");
			complexFooBarSurfObject.setPropertyValue("foo", "foo");
			complexFooBarSurfObject.setPropertyValue("bar", simpleFooBarSurfObject);

			assertThat(baos.toString(StandardCharsets.UTF_8.name()), equalTo(serializer.serialize(complexFooBarSurfObject)));
		}
	}

}
