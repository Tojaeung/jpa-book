package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable
@Getter
public class Address {
    private String city;
    private String street;
    private String zipcode;

    /*
     *  값 타입은 변경 불가능하게 설계되어야 한다.
     *  Setter를 제거하고 생성자를 이용해 초기화해서 변경 불가능하게 만든다.
     *  임베디드타입은 기본생성자가 필요하므로 기본 생성자는 protected을 설정해서 변경하지 못하게 한다.
     * */
    protected Address() {
    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
