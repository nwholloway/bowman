package uk.co.blackpepper.hal.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.web.util.UriComponentsBuilder;

import uk.co.blackpepper.hal.client.annotation.RemoteResource;

import static uk.co.blackpepper.hal.client.ReflectionSupport.setId;

public class Client<T> {

	private final Class<T> entityType;

	private final URI baseUri;
	
	private final ClientProxyFactory proxyFactory;

	private final RestOperations restOperations;

	public Client(Class<T> entityType, Configuration configuration, RestOperations restOperations) {
		this.entityType = entityType;
		this.baseUri = configuration.getBaseUri();
		this.proxyFactory = configuration.getProxyFactory();
		this.restOperations = restOperations;
	}

	public T get(URI uri) {
		return proxyFactory.create(uri, entityType, restOperations);
	}
	
	public Iterable<T> getAll() {
		List<T> result = new ArrayList<T>();
		
		Resources<Resource<T>> resources = restOperations.getResources(getEntityBaseUri(), entityType);
		
		for (Resource<T> resource : resources) {
			result.add(proxyFactory.create(resource, entityType, restOperations));
		}
		
		return result;
	}

	public URI post(T object) {
		URI resourceUri = restOperations.postObject(getEntityBaseUri(), object);
		
		setId(object, resourceUri);
		
		return resourceUri;
	}

	public void delete(URI uri) {
		restOperations.deleteResource(uri);
	}

	private URI getEntityBaseUri() {
		String path = entityType.getAnnotation(RemoteResource.class).value();
		
		return UriComponentsBuilder.fromUri(baseUri).path(path).build().toUri();
	}
}