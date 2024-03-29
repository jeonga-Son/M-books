package com.ll.exam.eBook.app.product.entity;

import com.ll.exam.eBook.app.AppConfig;
import com.ll.exam.eBook.app.base.entity.BaseEntity;
import com.ll.exam.eBook.app.cart.entity.CartItem;
import com.ll.exam.eBook.app.member.entity.Member;
import com.ll.exam.eBook.app.postkeyword.entity.PostKeyword;
import com.ll.exam.eBook.app.productTag.entity.ProductTag;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static javax.persistence.FetchType.LAZY;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Product extends BaseEntity {
    @ManyToOne(fetch = LAZY)
    private Member author;
    @ManyToOne(fetch = LAZY)
    private PostKeyword postKeyword;
    private String subject;
    private int price;

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.EXTRA)
    Set<ProductTag> productTags = new LinkedHashSet<>();

    public Product(long id) {
        super(id);
    }

    public int getSalePrice() {
        return getPrice();
    }

    public int getWholesalePrice() {
        return (int) Math.ceil(getPrice() * AppConfig.getWholesalePriceRate());
    }

    public boolean isOrderable() {
        return true;
    }

    public String getJdenticon() {
        return "product__" + getId();
    }

    public String getExtra_inputValue_hashTagContents() {
        return productTags
                .stream()
                .map(productTag -> "#" + productTag.getProductKeyword().getContent())
                .sorted()
                .collect(Collectors.joining(" "));
    }

    public String getExtra_productTagLinks() {
        return productTags
                .stream()
                .map(productTag -> {
                    String text = "#" + productTag.getProductKeyword().getContent();

                    return """
                            <a href="%s" class="text-link">%s</a>
                            """
                            .stripIndent()
                            .formatted(productTag.getProductKeyword().getListUrl(), text);
                })
                .sorted()
                .collect(Collectors.joining(" "));
    }

    public CartItem getExtra_actor_cartItem() {
        Map<String, Object> extra = getExtra();

        if (extra.containsKey("actor_cartItem") == false) {
            return null;
        }

        return (CartItem) extra.get("actor_cartItem");
    }

    public boolean getExtra_actor_hasInCart() {
        return getExtra_actor_cartItem() != null;
    }

    public void updateProductTags(Set<ProductTag> newProductTags) {
        // 지울거 모으고
        Set<ProductTag> needToDelete = productTags
                .stream()
                .filter(Predicate.not(newProductTags::contains))
                .collect(Collectors.toSet());

        // 모아진걸 지우고
        needToDelete
                .stream()
                .forEach(productTags::remove);

        // 넣을거 넣는다.
        // SET 이기 때문에 중복 신경쓰지 말고 넣는다.
        newProductTags
                .stream()
                .forEach(productTags::add);
    }
}
