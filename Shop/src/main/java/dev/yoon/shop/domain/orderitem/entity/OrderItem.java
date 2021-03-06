package dev.yoon.shop.domain.orderitem.entity;


import dev.yoon.shop.domain.base.BaseEntity;
import dev.yoon.shop.domain.base.BaseTimeEntity;
import dev.yoon.shop.domain.item.entity.Item;
import dev.yoon.shop.domain.order.entity.Order;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Table(name = "order_item")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice;

    private int count;

    @Builder
    public OrderItem(Long id, Item item, Order order, int orderPrice, int count) {
        this.id = id;
        this.item = item;
        this.order = order;
        this.orderPrice = orderPrice;
        this.count = count;
    }

    public static OrderItem createOrderItem(Item item, int count) {
        /**
         * 주문 상품을 생성한다는 것은 주문 수량만큼 상품의 재고를 차감하는 것
         */
        item.removeStock(count);

        return OrderItem.builder()
                .orderPrice(item.getPrice())
                .count(count)
                .item(item)
                .build();
    }
    public void updateOrder(Order orders) {
        this.order = orders;
    }

    public int getTotalPrice() {
        return this.getOrderPrice() * this.getCount();
    }

    public void cancel() {
        this.getItem().addStock(count);
    }

}
