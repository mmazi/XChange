package com.xeiam.xchange.proxy;

import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.utils.HttpTemplate;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
* @author Matija Mazi <br/>
*/
public class RestInvocationHandler implements InvocationHandler {
  private final HttpTemplate httpTemplate;
  private final ExchangeSpecification exchangeSpecification;
  private final ObjectMapper mapper;
  private final String intfacePath;

  public RestInvocationHandler(HttpTemplate httpTemplate, ExchangeSpecification exchangeSpecification, ObjectMapper mapper, Class<?> restInterface) {
    this.httpTemplate = httpTemplate;
    this.exchangeSpecification = exchangeSpecification;
    this.mapper = mapper;
    intfacePath = restInterface.getAnnotation(Path.class).value();
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Path path = method.getAnnotation(Path.class);
    Class<?> returnType = method.getReturnType();
    QueryStringBuilder params = new QueryStringBuilder();
    Annotation[][] paramAnnotations = method.getParameterAnnotations();
    boolean useGetMethod = method.isAnnotationPresent(GET.class);
    for (int i = 0; i < paramAnnotations.length; i++) {
      Annotation[] paramAnns = paramAnnotations[i];
      String paramName = getParamName(paramAnns, useGetMethod);
      params.add(paramName, args[i]);
    }
    if (useGetMethod) {
      return getForJsonObject(path.value(), returnType, params);
    } else {
      if (!method.isAnnotationPresent(POST.class)) {
        throw new IllegalArgumentException("Only methods annotated with @GET or @POST supported.");
      }
      return postForJsonObject(path.value(), returnType, params);
    }
  }

  private String getParamName(Annotation[] paramAnns, boolean useGetMethod) {
    Class<? extends Annotation> paramAnnClass = useGetMethod ? QueryParam.class : FormParam.class;
    Annotation queryParam = findElementOfClass(paramAnns, paramAnnClass);
    return useGetMethod ? QueryParam.class.cast(queryParam).value() : FormParam.class.cast(queryParam).value();
  }

  private static <T, U extends T> U findElementOfClass(T[] paramAnns, Class<U> classU) {
    for (T ann : paramAnns) {
      if (classU.isInstance(ann)) {
        //noinspection unchecked
        return (U) ann;
      }
    }
    throw new IllegalArgumentException("Cannot find element of class " + classU);
  }

  private String getUrl(String method) {

    // todo: make more robust in terms of path separator ('/') handling
    return String.format("%s/%s/%s", exchangeSpecification.getUri(), intfacePath , method);
  }

  protected <T> T getForJsonObject(String method, Class<T> returnType, QueryStringBuilder params) {

    String url = getUrl(method);
    if (params != null) {
      url += "?" + params.toString(true);
    }

    return httpTemplate.getForJsonObject(url, returnType, mapper, new HashMap<String, String>());
  }

  protected  <T> T postForJsonObject(String method, Class<T> returnType, QueryStringBuilder postBody) {

    return httpTemplate.postForJsonObject(getUrl(method), returnType, postBody.toString(false), mapper, new HashMap<String, String>());
  }
}