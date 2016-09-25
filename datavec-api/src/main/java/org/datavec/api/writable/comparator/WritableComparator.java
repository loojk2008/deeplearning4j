/*
 *  * Copyright 2016 Skymind, Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 */

package org.datavec.api.writable.comparator;

import org.nd4j.shade.jackson.annotation.JsonSubTypes;
import org.nd4j.shade.jackson.annotation.JsonTypeInfo;
import org.datavec.api.writable.Writable;

import java.io.Serializable;
import java.util.Comparator;

@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes(value= {
        @JsonSubTypes.Type(value = DoubleWritableComparator.class, name = "DoubleWritableComparator"),
        @JsonSubTypes.Type(value = FloatWritableComparator.class, name = "FloatWritableComparator"),
        @JsonSubTypes.Type(value = IntWritableComparator.class, name = "IntWritableComparator"),
        @JsonSubTypes.Type(value = LongWritableComparator.class, name = "LongWritableComparator"),
        @JsonSubTypes.Type(value = TextWritableComparator.class, name = "TextWritableComparator")
})
public interface WritableComparator extends Comparator<Writable>, Serializable {

}
