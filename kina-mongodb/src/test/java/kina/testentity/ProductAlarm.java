package kina.testentity;

import kina.annotations.Entity;
import kina.annotations.Field;
import kina.entity.KinaType;

import java.util.List;
import java.util.Map;

/**
 * Created by luca on 27/11/14.
 */
@Entity
public class ProductAlarm implements KinaType{

    @Field private boolean enabled;
    @Field private java.lang.Integer balanceThreshold;
    @Field private java.util.List<TransactionFilter> alarmTxFilter;
    @Field private java.util.Map<AlarmEventType,java.lang.Boolean> enabledByType;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getBalanceThreshold() {
        return balanceThreshold;
    }

    public void setBalanceThreshold(Integer balanceThreshold) {
        this.balanceThreshold = balanceThreshold;
    }

    public List<TransactionFilter> getAlarmTxFilter() {
        return alarmTxFilter;
    }

    @Override
    public String toString() {
        return "ProductAlarm{" +
                "\nenabled=" + enabled +
                "\n, balanceThreshold=" + balanceThreshold +
                "\n, alarmTxFilter=" + alarmTxFilter +
                "\n, enabledByType=" + enabledByType +
                "\n}";
    }

    public void setAlarmTxFilter(List<TransactionFilter> alarmTxFilter) {
        this.alarmTxFilter = alarmTxFilter;
    }

    public Map<AlarmEventType, Boolean> getEnabledByType() {
        return enabledByType;
    }

    public void setEnabledByType(Map<AlarmEventType, Boolean> enabledByType) {
        this.enabledByType = enabledByType;
    }
}
