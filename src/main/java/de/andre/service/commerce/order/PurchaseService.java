package de.andre.service.commerce.order;

import de.andre.entity.order.CommerceItem;
import de.andre.entity.order.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// purchase facade
public class PurchaseService {
    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);

    private final CommerceItemsTools commerceItemsTools;
    private final ShippingGroupsTools shippingGroupsTools;

    private OrderHolder orderHolder;

    public PurchaseService(final CommerceItemsTools commerceItemsTools, final ShippingGroupsTools shippingGroupsTools) {
        this.commerceItemsTools = commerceItemsTools;
        this.shippingGroupsTools = shippingGroupsTools;
    }

    public void modifyOrderItem(final String productId, final Long quantity) {
        final Order order = orderHolder.currentOrder();
        synchronized (order) {
            final CommerceItem ci = commerceItemsTools.modifyItemQuantity(order, productId, quantity);
            shippingGroupsTools.modifyShippingGroupQuantity(order, ci, quantity);
        }
    }
}
