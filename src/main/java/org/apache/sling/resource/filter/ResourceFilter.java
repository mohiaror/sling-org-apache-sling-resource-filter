/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.resource.filter;

import java.io.ByteArrayInputStream;
import java.util.function.Predicate;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.resource.filter.api.Context;
import org.apache.sling.resource.filter.api.impl.ComparisonVisitor;
import org.apache.sling.resource.filter.api.impl.DefaultContext;
import org.apache.sling.resource.filter.api.impl.LogicVisitor;
import org.apache.sling.resource.filter.impl.FilterParser;
import org.apache.sling.resource.filter.impl.ParseException;
import org.apache.sling.resource.filter.impl.node.Node;

/**
 * Creates a {@link Predicate} of type {@link Resource} to identify matching
 * Resource objects
 *
 */
public class ResourceFilter implements Predicate<Resource> {

    private Predicate<Resource> parsedPredicate;

    private Context context;

    /**
     * Creates a {@link Predicate<Resource>} using the input String as the basis of
     * the selection
     * 
     * @param matchDefinition
     *            Script used to define the matching requirements for the Predicate
     * @throws ParseException
     */
    public ResourceFilter(String matchDefinition) throws ParseException {
        Node rootNode = new FilterParser(new ByteArrayInputStream(matchDefinition.getBytes())).parse();
        this.parsedPredicate = rootNode.accept(getContext().getLogicVisitor());
    }

    /**
     * Creates a {@link Predicate<Resource>} using the input String as the basis of
     * the selection
     * 
     * @param matchDefinition
     *            Script used to define the matching requirements for the Predicate
     * @param charEncoding
     *            char encoding of the matchDefinition
     * @throws ParseException
     */
    public ResourceFilter(String matchDefinition, String charEncoding) throws ParseException {
        Node rootNode = new FilterParser(new ByteArrayInputStream(matchDefinition.getBytes()), charEncoding).parse();
        this.parsedPredicate = rootNode.accept(getContext().getLogicVisitor());
    }

    @Override
    public boolean test(Resource resource) {
        return parsedPredicate.test(resource);
    }

    public Context getContext() {
        if (context == null) {
            context = new DefaultContext();
            new LogicVisitor(context);
            new ComparisonVisitor(context);
        }
        return context;
    }

    public ResourceFilter setContext(Context context) {
        this.context = context;
        return this;
    }

    public ResourceFilter addArgument(String key, Object value) {
        getContext().addArgument(key, value);
        return this;
    }

}
