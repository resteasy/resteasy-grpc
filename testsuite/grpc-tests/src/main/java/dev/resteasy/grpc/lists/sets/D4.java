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

import java.util.List;
import java.util.Set;

public class D4 {

    L3<Integer> l00;
    L3<?> l01;
    L3<L1> l02;
    L3<L2> l03;
    L3<L3<String>> l04;

    L3<Set<Float>> l10;
    L3<List<?>> l11;
    L3<List<L1>> l12;
    L3<List<L2>> l13;
    L3<List<L3<Integer>>> l14;

    List<Integer> l20;
    List<?> l21;
    List<L1> l22;
    List<L2> l23;
    List<L3<Float>> l24;

    List<List<Float>> l30;
    List<List<?>> l31;
    List<List<L1>> l32;
    List<List<L2>> l33;
    List<List<L3<Long>>> l34;

    List<L3<Float>> l40;
    List<L3<?>> l41;
    List<L3<L1>> l42;
    List<L3<L2>> l43;
    List<L3<L3<Long>>> l44;
}
