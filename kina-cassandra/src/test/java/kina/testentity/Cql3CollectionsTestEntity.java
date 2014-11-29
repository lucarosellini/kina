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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import kina.annotations.Entity;
import kina.annotations.Field;
import kina.annotations.PartitionKey;
import kina.entity.KinaType;
import org.apache.cassandra.db.marshal.Int32Type;
import org.apache.cassandra.db.marshal.ListType;
import org.apache.cassandra.db.marshal.MapType;
import org.apache.cassandra.db.marshal.SetType;

/**
 * Created by luca on 24/03/14.
 */
@Entity
public class Cql3CollectionsTestEntity implements KinaType {

    @PartitionKey(validationClass = Int32Type.class)
    private Integer id;

    @Field(fieldName = "first_name")
    private String firstName;

    @Field(fieldName = "last_name")
    private String lastName;

    @Field(validationClass = SetType.class)
    private Set<String> emails;

    @Field(validationClass = ListType.class)
    private List<String> phones;

    @Field(validationClass = MapType.class)
    private Map<UUID, Integer> uuid2id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Set<String> getEmails() {
        return emails;
    }

    public void setEmails(Set<String> emails) {
        this.emails = emails;
    }

    public Map<UUID, Integer> getUuid2id() {
        return uuid2id;
    }

    public void setUuid2id(Map<UUID, Integer> uuid2id) {
        this.uuid2id = uuid2id;
    }

    public List<String> getPhones() {
        return phones;
    }

    public void setPhones(List<String> phones) {
        this.phones = phones;
    }

}
