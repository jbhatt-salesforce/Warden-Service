/* Copyright (c) 2015-2017, Salesforce.com, Inc.
 * All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *   
 *      Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 *      Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 *      Neither the name of Salesforce.com nor the names of its contributors may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

package com.salesforce.dva.warden.ws.dto;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import com.salesforce.dva.warden.dto.Base;
import com.salesforce.dva.warden.ws.resources.AbstractResource.Description;
import java.util.Arrays;
import java.util.Locale;

/**
 * Represents the help context for an endpoint method.
 *
 * @author  Tom Valine (tvaline@salesforce.com)
 */
public class MethodHelp implements Comparable<MethodHelp> {

    private String _path;
    private String _description;
    private String _method;
    private String[] _produces;
    private String[] _consumes;
    private List<MethodParameterDto> _params;

    private static String _getHttpMethod(Method method) {
        if (method.getAnnotation(GET.class) != null) {
            return "GET";
        }

        if (method.getAnnotation(POST.class) != null) {
            return "POST";
        }

        if (method.getAnnotation(PUT.class) != null) {
            return "PUT";
        }

        if (method.getAnnotation(DELETE.class) != null) {
            return "DELETE";
        }

        return null;
    }

    private static List<MethodParameterDto> _getMethodParams(Method method) {
        List<MethodParameterDto> result = new ArrayList<>();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Class<?>[] parameterTypes = method.getParameterTypes();

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            Annotation[] annotations = parameterAnnotations[i];

            if (annotations.length > 0) {
                for (Annotation annotation : annotations) {
                    if (!Context.class.isAssignableFrom(annotation.getClass())) {
                        MethodParameterDto param = new MethodParameterDto();

                        if (QueryParam.class.isAssignableFrom(annotation.getClass())) {
                            QueryParam queryParam = QueryParam.class.cast(annotation);

                            param.setName(queryParam.value());
                            param.setParamType("query");
                        } else if (FormParam.class.isAssignableFrom(annotation.getClass())) {
                            FormParam queryParam = FormParam.class.cast(annotation);

                            param.setName(queryParam.value());
                            param.setParamType("form");
                        } else if (PathParam.class.isAssignableFrom(annotation.getClass())) {
                            PathParam queryParam = PathParam.class.cast(annotation);

                            param.setName(queryParam.value());
                            param.setParamType("path");
                        }

                        try {
                            if (Base.class.isAssignableFrom(parameterType)) {
                                param.setSchema(Base.class.cast(parameterType.newInstance()).createExample());
                            }
                        } catch (InstantiationException | IllegalAccessException ex) {
                            param.setSchema(parameterType);
                        }

                        String paramName = parameterType.getSimpleName().toLowerCase();

                        param.setDataType(paramName.toLowerCase().replaceAll("dto", ""));
                        result.add(param);
                    }
                }
            } else {
                MethodParameterDto param = new MethodParameterDto();
                String paramName = parameterType.getSimpleName().toLowerCase();

                param.setDataType(paramName.toLowerCase().replaceAll("dto", ""));
                param.setName("body");
                param.setParamType("body");

                try {
                    if (Base.class.isAssignableFrom(parameterType)) {
                        param.setSchema(Base.class.cast(parameterType.newInstance()).createExample());
                    }
                } catch (InstantiationException | IllegalAccessException ex) {
                    param.setSchema(parameterType);
                }

                result.add(param);
            }    // end if-else
        }    // end for

        return result;
    }

    @Override
    public int compareTo(MethodHelp o) {
        int pathCompare = String.CASE_INSENSITIVE_ORDER.compare(_path, o._path);
        int methodCompare = String.CASE_INSENSITIVE_ORDER.compare(_method, o._method);

        return (pathCompare == 0) ? methodCompare : pathCompare;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final MethodHelp other = (MethodHelp) obj;

        if (!Objects.equals(_path, other._path)) {
            return false;
        }

        if (!Objects.equals(_description, other._description)) {
            return false;
        }

        if (!Objects.equals(_method, other._method)) {
            return false;
        }

        return true;
    }

    /**
     * Creates a method help DTO from a method object.
     *
     * @param   parentPath  The parent method path.
     * @param   method      The method to convert.
     *
     * @return  The method help DTO.
     */
    public static MethodHelp fromMethodClass(String parentPath, Method method) {
        String methodName = _getHttpMethod(method);
        Path path = method.getAnnotation(Path.class);
        Description description = method.getAnnotation(Description.class);
        Produces produces = method.getAnnotation(Produces.class);
        Consumes consumes = method.getAnnotation(Consumes.class);

        if (((path == null) ||!path.value().contains("help")) && (description != null)) {
            String relativePath = (path == null) ? "" : path.value();
            String fullPath = (parentPath == null) ? relativePath : parentPath + relativePath;
            MethodHelp result = new MethodHelp();

            result.setDescription(description.value());
            result.setMethod(methodName);
            result.setPath(fullPath);

            if (produces != null) {
                result.setProduces(produces.value());
            }

            if (consumes != null) {
                result.setConsumes(consumes.value());
            }

            List<MethodParameterDto> params = _getMethodParams(method);

            result.setParams(params);

            return result;
        } else {
            return null;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = 67 * hash + Objects.hashCode(_path);
        hash = 67 * hash + Objects.hashCode(_description);
        hash = 67 * hash + Objects.hashCode(_method);

        return hash;
    }

    /**
     * Returns the list of media types this method consumes.
     *
     * @return  The list of media types this method consumes.
     */
    public String[] getConsumes() {
        return _consumes == null ? null : Arrays.copyOf(_consumes, _consumes.length);
    }

    /**
     * Sets the list of media types this method consumes.
     *
     * @param  consumes  The list of media types this method consumes.
     */
    public void setConsumes(String[] consumes) {
        _consumes = consumes == null ? null : Arrays.copyOf(consumes, consumes.length);
    }

    /**
     * Returns the method description.
     *
     * @return  The method description.
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Sets the method description.
     *
     * @param  description  The method description.
     */
    public void setDescription(String description) {
        _description = description;
    }

    /**
     * Returns the method name.
     *
     * @return  The method name.
     *
     */
    public String getMethod() {
        return _method;
    }

    /**
     * Sets the method name.
     *
     * @param  method  The method name.
     */
    public void setMethod(String method) {
        _method = method;
    }

    /**
     * Returns the list of parameters the method requires.
     *
     * @return  The list of parameters the method requires.
     */
    public List<MethodParameterDto> getParams() {
        return _params;
    }

    /**
     * Sets the list of parameters the method requires.
     *
     * @param  params  The list of parameters the method requires.
     */
    public void setParams(List<MethodParameterDto> params) {
        _params = params;
    }

    /**
     * Returns the method path.
     *
     * @return  The method path.
     */
    public String getPath() {
        return _path;
    }

    /**
     * Sets the method path.
     *
     * @param  path  The method path.
     */
    public void setPath(String path) {
        _path = path;
    }

    /**
     * Returns the list of media types this method produces.
     *
     * @return  The list of media types this method produces.
     */
    public String[] getProduces() {
        return _produces == null ? null : Arrays.copyOf(_produces, _produces.length);
    }

    /**
     * Sets the list of media types this method produces.
     *
     * @param  produces  The list of media types this method produces.
     */
    public void setProduces(String[] produces) {
        _produces = produces == null ? null : Arrays.copyOf(produces, produces.length);
    }

    /**
     * Represents a method parameter.
     *
     * @author  Tom Valine (tvaline@salesforce.com)
     */
    public static class MethodParameterDto {

        String paramType;
        String name;
        String dataType;
        Object schema;

        /**
         * Returns the data type.
         *
         * @return  The data type.
         *
         */
        public String getDataType() {
            return dataType;
        }

        /**
         * Sets the data type.
         *
         * @param  dataType  The data type.
         */
        public void setDataType(String dataType) {
            this.dataType = dataType;
        }

        /**
         * Returns the parameter name.
         *
         * @return  The parameter name.
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the parameter name.
         *
         * @param  name  The parameter name.
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Returns the parameter type.
         *
         * @return  The parameter type.
         */
        public String getParamType() {
            return paramType;
        }

        /**
         * Sets the parameter type.
         *
         * @param  paramType  The parameter type.
         */
        public void setParamType(String paramType) {
            this.paramType = paramType;
        }

        /**
         * Returns a representation of the schema of the parameter.
         *
         * @return  The parameter schema.
         */
        public Object getSchema() {
            return schema;
        }

        /**
         * Sets the parameter data type schema.
         *
         * @param  schema  The schema.
         */
        public void setSchema(Object schema) {
            this.schema = schema;
        }

    }

}

/* Copyright (c) 2015-2017, Salesforce.com, Inc.  All rights reserved. */



