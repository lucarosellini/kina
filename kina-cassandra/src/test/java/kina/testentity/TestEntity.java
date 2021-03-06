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

import kina.annotations.Entity;
import kina.annotations.Field;
import org.apache.cassandra.db.marshal.Int32Type;
import org.apache.cassandra.db.marshal.LongType;

/**
 * Entity test class, inherits field from {@link BaseTestEntity}
 */
@Entity
public class TestEntity extends BaseTestEntity {
    private static final long serialVersionUID = -6242942929275890323L;

    @Field
    private String url;

    @Field(validationClass = Int32Type.class, fieldName = "response_time")
    private Integer responseTime;

    @Field(fieldName = "response_code", validationClass = Int32Type.class)
    private Integer responseCode;

    @Field(validationClass = LongType.class, fieldName = "download_time")
    private Long downloadTime;

    private String notMappedField;

    public TestEntity() {
        super();
    }

    public TestEntity(String id, String domain, String url, Integer responseTime, Integer responseCode,
                      String notMappedField) {
        super(domain);
        setId(id);
        this.url = url;
        this.responseTime = responseTime;
        this.responseCode = responseCode;
        this.downloadTime = null;
        this.notMappedField = notMappedField;
    }

    public Long getDownloadTime() {
        return downloadTime;
    }


    public String getNotMappedField() {
        return notMappedField;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public Integer getResponseTime() {
        return responseTime;
    }

    public String getUrl() {
        return url;
    }

    public void setDownloadTime(Long downloadTime) {
        this.downloadTime = downloadTime;
    }

    public void setNotMappedField(String notMappedField) {
        this.notMappedField = notMappedField;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public void setResponseTime(Integer responseTime) {
        this.responseTime = responseTime;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "TestEntity [" + (getId() != null ? "id=" + getId() + ", " : "")
                + (getDomain() != null ? "domain=" + getDomain() + ", " : "") + (url != null ? "url=" + url + ", " : "")
                + (responseTime != null ? "responseTime=" + responseTime + ", " : "")
                + (responseCode != null ? "responseCode=" + responseCode + ", " : "")
                + (downloadTime != null ? "downloadTime=" + downloadTime + ", " : "")
                + (notMappedField != null ? "notMappedField=" + notMappedField : "") + "]\n";
    }
}
