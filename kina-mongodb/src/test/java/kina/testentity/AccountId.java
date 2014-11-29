package kina.testentity;

import kina.annotations.Entity;
import kina.annotations.Field;
import kina.entity.KinaType;

/**
 * Created by luca on 27/11/14.
 */
@Entity
public class AccountId implements KinaType{
    @Field private java.lang.String bank;
    @Field private java.lang.String branch;
    @Field private java.lang.String controlDigits;
    @Field private java.lang.String number;

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getControlDigits() {
        return controlDigits;
    }

    public void setControlDigits(String controlDigits) {
        this.controlDigits = controlDigits;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
