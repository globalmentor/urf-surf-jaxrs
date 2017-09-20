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

package io.urf.surf.jaxrs;

import javax.annotation.*;

/**
 * The most basic of JavaBean implementations. A pure bean.
 * <p>
 * The difference from this class and {@link SimpleFooBarBean} is only that the property <code>bar<code> is an instance of {@link SimpleFooBarBean}.
 * </p>
 * 
 * @author Magno Nascimento
 */
public class ComplexFooBarBean {

	private String foo;

	private SimpleFooBarBean bar;

	/** No-args constructor. */
	public ComplexFooBarBean() {
	}

	/** @return The foo. */
	public String getFoo() {
		return foo;
	}

	/**
	 * Sets foo.
	 * @param foo The new foo.
	 */
	public void setFoo(@Nullable final String foo) {
		this.foo = foo;
	}

	/** @return The bar. */
	public SimpleFooBarBean getBar() {
		return bar;
	}

	/**
	 * Sets bar.
	 * 
	 * @param bar The new bar.
	 */
	public void setBar(@Nullable final SimpleFooBarBean bar) {
		this.bar = bar;
	}

}
