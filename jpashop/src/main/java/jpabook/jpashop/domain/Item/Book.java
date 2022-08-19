package jpabook.jpashop.domain.Item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("책")
@Getter
@Setter
public class Book extends Item {
    private String author;
    private String isbn;
}
