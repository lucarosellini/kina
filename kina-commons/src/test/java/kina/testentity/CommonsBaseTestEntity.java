/*
 * Copyright 2014, Luca Rosellini.
 *
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

package kina.testentity;

import kina.annotations.Field;
import kina.annotations.Key;
import kina.entity.KinaType;

/**
 * Created by luca on 23/04/14.
 */
public class CommonsBaseTestEntity implements KinaType {
    @Key
    protected String id;
    @Field(fieldName = "domain_name")
    protected String domain;

    public CommonsBaseTestEntity(String id, String domain) {
        this.id = id;
        this.domain = domain;
    }

    public CommonsBaseTestEntity() {

    }

    public String getDomain() {
        return domain;
    }

    public String getId() {
        return id;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setId(String id) {
        this.id = id;
    }
}
