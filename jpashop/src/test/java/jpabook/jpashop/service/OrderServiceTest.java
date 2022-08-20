package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception {
        // given
        Member member = createMember();

        Item book = createBook("안뇽", 1000, 10);

        int orderCount = 2;

        // when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        // then
        Order getOrder = orderRepository.findOne(orderId);

        Assertions.assertEquals(OrderStatus.ORDER, getOrder.getStatus());
        Assertions.assertEquals(1, getOrder.getOrderItems().size());
        Assertions.assertEquals(10000 * orderCount, getOrder.getTotalPrice());
        Assertions.assertEquals(8, book.getStockQuantity());
    }

    @Test
    public void 상품주문_재고수량초과() throws Exception {
        // given
        Member member = createMember();
        Item item = createBook("안뇽", 10000, 10);

        int orderCount = 10;

        // when
        try {
            orderService.order(member.getId(), item.getId(), orderCount);
        } catch (NotEnoughStockException e) {
            return;
        }

        // then
        Assertions.fail("재고 수량 부족 예외가 발생해야한다.");
    }

    @Test
    public void 주문취소() throws Exception {
        // given
        Member member = createMember();
        Item item = createBook("안뇽", 10000, 10);

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        // when
        orderService.cancelOrder(orderId);

        // then
        Order getOrder = orderRepository.findOne(orderId);

        Assertions.assertEquals(OrderStatus.CANCEL, getOrder.getStatus());
        Assertions.assertEquals(10, item.getStockQuantity());


    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "경기", "12345"));
        em.persist(member);
        return member;
    }

    private Item createBook(String name, int price, int stockQuantity) {
        Item book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }
}