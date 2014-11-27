package kina.testentity;

import kina.annotations.Entity;
import kina.annotations.Field;
import kina.entity.KinaType;

import java.util.List;

/**
 * Created by luca on 27/11/14.
 */
@Entity
public class TransactionFilter implements KinaType {
    @Field private java.lang.Integer threshold;
    @Field private java.util.List<java.lang.String> categories;

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    @Override
    public String toString() {
        return "TransactionFilter{" +
                "\nthreshold=" + threshold +
                "\n, categories=" + categories +
                "\n}";
    }
}
