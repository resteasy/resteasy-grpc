/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2025 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
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
package dev.resteasy.grpc.lists.sets;

import java.util.Collection;

public class CollectionEquals {

    public static boolean equals(Object o1, Object o2) {
        if (!o1.getClass().equals(o2.getClass())) {
            return false;
        }
        if (!Collection.class.isAssignableFrom(o1.getClass()) || !Collection.class.isAssignableFrom(o2.getClass())) {
            return false;
        }
        Object[] o1s = ((Collection) o1).toArray();
        ;
        Object[] o2s = ((Collection) o2).toArray();
        ;
        if (o1s.length != o2s.length) {
            return false;
        }
        for (int i = 0; i < o1s.length; i++) {
            Object o1s1 = o1s[i];
            Object o2s1 = o2s[i];
            if (Collection.class.isAssignableFrom(o1s1.getClass()) && Collection.class.isAssignableFrom(o2s1.getClass())) {
                return equals(o1s1, o2s1);
            }
            if ((o1s1 == null && o2s1 != null) || (o1s1 != null && o2s1 == null)) {
                return false;
            }
            if (o1s1 == null) {
                continue;
            }
            if (!o1s1.equals(o2s1)) {
                return false;
            }
        }
        return true;
    }
}
