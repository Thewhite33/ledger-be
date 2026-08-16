package com.example.ledger.specification;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import com.example.ledger.entity.Customer;

import java.util.ArrayList;
import java.util.List;

public final class CustomerSpecifications {

	private CustomerSpecifications() {
	}

	public static Specification<Customer> filter(Long userId, String search) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			predicates.add(
					criteriaBuilder.equal(
							root.get("user").get("id"),
							userId));

			if (search != null && !search.isBlank()) {
				String pattern = "%" + search.trim().toLowerCase() + "%";

				Expression<String> phoneExpression = criteriaBuilder.coalesce(
						root.get("phone"),
						"");

				predicates.add(
						criteriaBuilder.or(
								criteriaBuilder.like(
										criteriaBuilder.lower(root.get("name")),
										pattern),
								criteriaBuilder.like(
										criteriaBuilder.lower(phoneExpression),
										pattern)));
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
	}
}