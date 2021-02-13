package com.springfilter.node;

import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import com.springfilter.compiler.node.INode;
import com.springfilter.compiler.token.IToken;
import com.springfilter.token.input.IInput;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
public class Input implements IToken, IExpression {

  private IInput value;

  private Class<?> targetClass;

  @Override
  public INode transform(INode parent) {
    return this;
  }

  @Override
  public String generate() {
    return value.toStringAs(targetClass);
  }

  @Override
  public Expression<?> generate(Root<?> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder,
      Map<String, Join<Object, Object>> joins) {
    return criteriaBuilder.literal(value.getValueAs(targetClass));
  }

}
