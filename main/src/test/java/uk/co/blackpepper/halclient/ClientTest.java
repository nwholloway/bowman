/*
 * Copyright 2016 Black Pepper Software
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
package uk.co.blackpepper.halclient;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;

import uk.co.blackpepper.halclient.annotation.RemoteResource;
import uk.co.blackpepper.halclient.annotation.ResourceId;

import static java.util.Arrays.asList;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ClientTest {

	@RemoteResource("/entities")
	public static class Entity {
		
		private URI id;
		
		@ResourceId
		public URI getId() {
			return id;
		}
	}
	
	private static final String BASE_URI = "http://www.example.com";
	
	private Client<Entity> client;
	
	private RestOperations restOperations;

	private ClientProxyFactory proxyFactory;
	
	@Before
	public void setup() {
		restOperations = mock(RestOperations.class);
		proxyFactory = mock(ClientProxyFactory.class);
		
		Configuration config = new Configuration()
				.setBaseUri(BASE_URI);
		
		client = new Client<>(Entity.class, config, restOperations, proxyFactory);
	}
	
	@Test
	public void getReturnsProxy() {
		Entity expected = new Entity();
		
		Resource<Entity> resource = new Resource<>(new Entity());
		when(restOperations.getResource(URI.create("http://www.example.com/1"), Entity.class)).thenReturn(resource);
		when(proxyFactory.create(resource, Entity.class, restOperations)).thenReturn(expected);
		
		Entity proxy = client.get(URI.create("http://www.example.com/1"));
		
		assertThat(proxy, is(expected));
	}
	
	@Test
	public void getReturnsNullWhenRestOperationsReturnsNull() {
		when(restOperations.getResource(URI.create("http://www.example.com/1"), Entity.class)).thenReturn(null);
		
		Entity proxy = client.get(URI.create("http://www.example.com/1"));
		
		assertThat(proxy, is(nullValue()));
	}
	
	@Test
	public void getAllWithNoArgumentsReturnsProxyIterable() {
		Entity expected = new Entity();
		
		Resource<Entity> resource = new Resource<>(new Entity());
		when(restOperations.getResources(URI.create(BASE_URI + "/entities"), Entity.class)).thenReturn(
				new Resources<>(asList(resource)));
		when(proxyFactory.create(resource, Entity.class, restOperations)).thenReturn(expected);
		
		Iterable<Entity> proxies = client.getAll();
		
		assertThat(proxies, contains(expected));
	}

	@Test
	public void postReturnsId() {
		Entity entity = new Entity();
		when(restOperations.postObject(URI.create(BASE_URI + "/entities"), entity)).thenReturn(
				URI.create("http://www.example.com/1"));
		
		URI uri = client.post(entity);
		
		assertThat(uri, is(URI.create("http://www.example.com/1")));
	}
	
	@Test
	public void postSetsId() {
		Entity entity = new Entity();
		when(restOperations.postObject(URI.create(BASE_URI + "/entities"), entity)).thenReturn(
				URI.create("http://www.example.com/1"));
		
		client.post(entity);
		
		assertThat(entity.getId(), is(URI.create("http://www.example.com/1")));
	}
	
	@Test
	public void deleteInvokesRestOperations() {
		client.delete(URI.create("http://www.example.com/1"));
		
		verify(restOperations).deleteResource(URI.create("http://www.example.com/1"));
	}
}
