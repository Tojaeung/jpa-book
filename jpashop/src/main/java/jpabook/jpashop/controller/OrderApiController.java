package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToMany (OneToMany)
 * Order -> OrderItem
 */
@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }

        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

        // 지연로딩 N+1문제로 쿼리가 많이 나가는 문제
        return collect;
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> collect = orders.stream()
                .map(order -> new OrderDto(order))
                .collect(Collectors.toList());

        /*
         * 컬렉션(일대다)을 조회할때 디비에서 데이터가 2배로 뻥튀기되서 온다.
         * jpa에서는 distict를 입력해서 id(식별자)가 같으면 중복을 제거해준다. (디비는 아니다...)
         * 어쨋든 디비에서 어플로 뻥튀기된 데이터가 넘어가긴한다..
         * 페치 조인으로 쿼리 1번밖에 안나감
         * 그러나 !! 페이징 불가능 (메모리에서 페이징 처리함... 매우위험)
         * */
        return collect;
    }

    @GetMapping("/api/v4/orders")
    public List<OrderDto> ordersV4() {
        // xtoOne을 페치조인이 되어있다. 미리 가져온다.
        List<Order> orders = orderRepository.findAllWithMemberDelivery();

        List<OrderDto> collect = orders.stream()
                .map(order -> new OrderDto(order))
                .collect(Collectors.toList());

        /*
         * xToOne도 batch_size로 가져올수도 있지만 보내는 쿼리가 많아지기 떄문에 패치조인으로 한다.
         * 또한, 페치조인으로 최적화가 가능하다.
         * xToMany는 batch_size를 이용해서 최적화한다.
         * batch_size를 너무 작게해도 쿼리가 빈번히 나가기때문에 좋지않다. (100 ~ 1000)
         * */
        return collect;
    }


    @Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());
        }
    }

    @Data
    static class OrderItemDto {

        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

}