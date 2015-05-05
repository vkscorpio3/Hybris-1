package de.andre.service.commerce;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.andre.entity.catalog.DcsProduct;
import de.andre.entity.dto.PricedProductDTO;
import de.andre.entity.enums.AmountType;
import de.andre.entity.order.DcsppAmountInfo;
import de.andre.entity.order.DcsppItem;
import de.andre.entity.order.DcsppOrder;
import de.andre.repository.CommerceItemRepository;
import de.andre.repository.PriceRepository;
import de.andre.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andreika on 5/3/2015.
 */

@Service
public class CommerceItemTools {
	private final static Logger logger = LoggerFactory.getLogger(CommerceItemTools.class);

	private final ObjectMapper objectMapper;
	private final ProductRepository productRepository;
	private final CommerceItemRepository commerceItemRepository;
	private final OrderManager orderManager;
	private final PriceRepository priceRepository;

	@Autowired
	public CommerceItemTools(final ObjectMapper objectMapper, final ProductRepository productRepository,
							 final CommerceItemRepository commerceItemRepository, final OrderManager orderManager,
							 final PriceRepository priceRepository) {
		this.objectMapper = objectMapper;
		this.productRepository = productRepository;
		this.commerceItemRepository = commerceItemRepository;
		this.orderManager = orderManager;
		this.priceRepository = priceRepository;
	}

	@Transactional
	public ObjectNode addItemToOrder(final Integer pId, final DcsppOrder pOrder, final Integer quantity) {
		if (null == pId || null == pOrder || null == quantity) {
			throw new IllegalArgumentException("Incorrect params adding item.");
		}
		final ObjectNode response = objectMapper.createObjectNode().put("success", true);

		try {
			for (final DcsppItem commerceItem : pOrder.getCommerceItems()) {
				if (commerceItem.getProduct().getProductId().equals(pId)) {
					Integer currentQuantity = commerceItem.getQuantity();
					if (currentQuantity + quantity < 8) {
						commerceItem.setQuantity(currentQuantity + quantity);
					} else {
						response.put("success", false)
								.put("message", "Quantity is too big.");
						return response;
					}
				}
			}

			DcsProduct commerceProduct = productRepository.getOne(pId);
			if (null == commerceProduct) {
				logger.error("Didn't found product for pid {0}", pId);
				response.put("success", false)
						.put("message", "Product wasn't found.");
				return response;
			}
			pOrder.addCommerceItem(createCommerceItemFromProduct(commerceProduct, quantity));
			orderManager.persistOrder(pOrder);
		} catch (Exception e) {
			logger.error(e.toString());
			response.put("success", false);
		}

		return response;
	}

	private DcsppItem createCommerceItemFromProduct(final DcsProduct commerceProduct, final Integer quantity) {
		DcsppItem commerceItem = new DcsppItem();
		commerceItem.setQuantity(quantity);
		commerceItem.setProduct(commerceProduct);

		DcsppAmountInfo amountInfo = new DcsppAmountInfo();
		amountInfo.setCurrencyCode("EUR");
		amountInfo.setQuantity(quantity);
		amountInfo.setType(AmountType.ITEM_PRICE_INFO);

		final List<DcsProduct> product = new ArrayList<>(1);
		product.add(commerceProduct);
		final List<PricedProductDTO> itemPrices = priceRepository.getProductsPrices(product);
		if (null != itemPrices) {
			Double leastPrice = itemPrices.get(0).getPrice();
			for (final PricedProductDTO price : itemPrices) {
				if (price.getPrice() < leastPrice) {
					leastPrice = price.getPrice();
				}
			}
			amountInfo.setRawAmount(leastPrice*quantity);
			amountInfo.setAmount(leastPrice*quantity);
		} else {
			logger.warn("No price was found for {0}", commerceProduct.getProductId());
		}

		commerceItem.setAmountInfo(amountInfo);
		return commerceItem;
	}

	@Transactional
	public ObjectNode decreaseCommerceItemQuantity(final DcsppOrder pOrder, final Integer cId, final Integer quantity) {
		final ObjectNode response = objectMapper.createObjectNode().put("success", true);
		try {
			for (final DcsppItem commerceItem : pOrder.getCommerceItems()) {
				if (commerceItem.getCommerceItemId().equals(cId)) {
					if (quantity >= commerceItem.getQuantity()) {
						return removeCommerceItemFromOrder(pOrder, cId);
					} else {
						commerceItem.setQuantity(commerceItem.getQuantity() - quantity);
						DcsppAmountInfo amountInfo = commerceItem.getAmountInfo();
//						amountInfo.setRawAmount(amountInfo.getRawAmount());
						orderManager.persistOrder(pOrder);
					}
					break;
				}
			}
		} catch (Exception e) {
			logger.error(e.toString());
			response.put("success", false);
		}
		return response;
	}

	@Transactional
	public ObjectNode removeCommerceItemFromOrder(final DcsppOrder pOrder, final Integer cId) {
		if (null == pOrder || null == cId) {
			throw new IllegalArgumentException("Invalid params removing item.");
		}
		final ObjectNode response = objectMapper.createObjectNode().put("success", true);
		try {
			for (final DcsppItem commerceItem : pOrder.getCommerceItems()) {
				if (commerceItem.getCommerceItemId().equals(cId)) {
					pOrder.removeCommerceItem(commerceItem);
					break;
				}
			}
			orderManager.persistOrder(pOrder);
			return response;
		} catch (Exception e) {
			logger.error(e.toString());
			response.put("success", false)
					.put("message", "Error persisting order.");
			return response;
		}
	}
}