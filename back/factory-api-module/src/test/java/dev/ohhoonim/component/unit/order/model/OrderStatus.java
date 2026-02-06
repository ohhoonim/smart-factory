package dev.ohhoonim.component.unit.order.model;

import dev.ohhoonim.component.unit.MasterCode;

public enum OrderStatus implements MasterCode {
    INIT("ORD_STAT", "01", "ko"), // 주문 생성/초기
    CONFIRMED("ORD_STAT", "02", "ko"), // 주문 확정
    CANCELLED("ORD_STAT", "03", "ko"), // 주문 취소
    PAID("ORD_STAT", "04", "ko"); // 결제 완료

    private final String groupCode;
    private final String masterCode;
    private final String langCode;

    OrderStatus(String groupCode, String masterCode, String langCode) {
        this.groupCode = groupCode;
        this.masterCode = masterCode;
        this.langCode = langCode;
    }

    @Override
    public String groupCode() {
        return groupCode;
    }

    @Override
    public String masterCode() {
        return masterCode;
    }

    @Override
    public String langCode() {
        return langCode;
    }
}