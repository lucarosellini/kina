package kina.testentity;

import kina.annotations.Entity;
import kina.annotations.Field;
import kina.entity.KinaType;

/**
 * Created by luca on 27/11/14.
 */
@Entity
public class AccountEntity implements KinaType {
    @Field private java.lang.String alias;
    @Field private AccountId accountId;
    @Field private java.lang.String ccc;
    @Field private ProductAlarm accountAlarm;
    @Field private boolean canceled;
    private java.lang.String userAlias;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public AccountId getAccountId() {
        return accountId;
    }

    public void setAccountId(AccountId accountId) {
        this.accountId = accountId;
    }

    public String getCcc() {
        return ccc;
    }

    public void setCcc(String ccc) {
        this.ccc = ccc;
    }

    public ProductAlarm getAccountAlarm() {
        return accountAlarm;
    }

    public void setAccountAlarm(ProductAlarm accountAlarm) {
        this.accountAlarm = accountAlarm;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public String getUserAlias() {
        return userAlias;
    }

    public void setUserAlias(String userAlias) {
        this.userAlias = userAlias;
    }


    @Override
    public String toString() {
        return "AccountEntity{" +
                "\naccountAlarm=" + accountAlarm +
                "\n, accountId=" + accountId +
                "\n, alias='" + alias + '\'' +
                "\n, canceled=" + canceled +
                "\n, ccc='" + ccc + '\'' +
                "\n, userAlias='" + userAlias + '\'' +
                "\n}";
    }
}
