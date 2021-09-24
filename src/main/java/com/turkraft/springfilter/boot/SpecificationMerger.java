package com.turkraft.springfilter.boot;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class SpecificationMerger<T> implements Specification<T> {

  private static final long serialVersionUID = 1L;

  private List<Specification<T>> specifications;

  private Map<String, Join<?, ?>> joins;

  public SpecificationMerger() {
    this(new HashMap<String, Join<?, ?>>());
  }

  public SpecificationMerger(Map<String, Join<?, ?>> joins) {
    this.joins = joins;
  }

  @Override
  public Predicate toPredicate(
      Root<T> root,
      CriteriaQuery<?> query,
      CriteriaBuilder criteriaBuilder) {

    Specification<T> result = null;

    for (Specification<T> specification : specifications) {

      if (specification == null) {
        continue;
      }

      if (specification instanceof FilterSpecification) {
        ((FilterSpecification<T>) specification).setJoins(joins);
      }

      result = result == null ? specification : specification.and(result);

    }

    if (result == null) {
      return null;
    }

    return result.toPredicate(root, query, criteriaBuilder);

  }

  public List<Specification<T>> getSpecifications() {
    if (specifications == null) {
      specifications = new LinkedList<Specification<T>>();
    }
    return specifications;
  }

  public void setSpecifications(List<Specification<T>> specifications) {
    this.specifications = specifications;
  }

}
