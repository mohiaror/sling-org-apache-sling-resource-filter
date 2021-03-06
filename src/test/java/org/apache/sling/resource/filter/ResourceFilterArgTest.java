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

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.resource.filter.impl.ResourcePredicateImpl;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ResourceFilterArgTest {

    private static final String START_PATH = "/content/sample/en";
    @Rule
    public final SlingContext context = new SlingContext();

    Resource resource;
    
    ResourcePredicates resourceFilter = new ResourcePredicateImpl();
    
    @Before
    public void setUp() throws ParseException {
        context.load().json("/data.json", "/content/sample/en");
        resource = context.resourceResolver().getResource(START_PATH);
        context.registerAdapter(Resource.class, ResourceFilterStream.class, new ResourceFilterStream(resource,new ResourcePredicateImpl()));
        context.registerAdapter(Resource.class, ResourcePredicates.class, new ResourcePredicateImpl());
    }

    @Test
    public void testMatchingNameInArg() throws Exception {
        Map<String,Object> params = new HashMap<>();
        params.put("date", "2013-08-08T16:32:59");
        params.put("lang", "Mongolian");
        ResourceFilterStream rfs = resource.adaptTo(ResourceFilterStream.class);
        List<Resource> found = rfs.addParams(params).setChildSelector("[jcr:content/jcr:title] == $lang").stream().collect(Collectors.toList());
        assertEquals(1, found.size());
    }

    @Test
    public void testMatchingNameAndMultipleArgs() throws Exception {
        Map<String,Object> params = new HashMap<>();
        params.put("date", "2013-08-08T16:32:59");
        params.put("lang", "Mongolian");
        ResourceFilterStream rfs = resource.adaptTo(ResourceFilterStream.class);
        long size = rfs.addParams(params).setChildSelector("[jcr:content/created] > $date and [jcr:content/jcr:title] == $lang").stream().count();
        assertEquals(1, size);
    }

    @Test
    public void testNameFunctionAgainstRegex() throws ParseException, Exception {
        Predicate<Resource> filter = resourceFilter.withParameter("regex", "testpage[1-2]").parse("name() like $regex");
        List<Resource> found = handle(new ResourceStream(resource), filter);
        assertEquals(2, found.size());
    }

    private List<Resource> handle(ResourceStream streamer, Predicate<Resource> childSelector) {
        return streamer.stream(r -> true).filter(childSelector).collect(Collectors.toList());
    }

}
