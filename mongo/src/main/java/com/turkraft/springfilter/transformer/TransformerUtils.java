package com.turkraft.springfilter.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.turkraft.springfilter.helper.FieldTypeResolver;
import com.turkraft.springfilter.parser.node.FieldNode;
import com.turkraft.springfilter.parser.node.FilterNode;
import com.turkraft.springfilter.parser.node.InfixOperationNode;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class TransformerUtils {

    private final FieldTypeResolver fieldTypeResolver;
    private final Map<String, JsonNode> cachedNodes = new HashMap<>();
    private final Set<String> cachedPaths = new HashSet<>();

    public JsonNode wrapArrays(FilterJsonNodeTransformer transformer, JsonNode node, InfixOperationNode source) {
        if (!(source.getLeft() instanceof FieldNode)) {
            return node;
        }

        String fullPath = transformer.transform(source.getLeft()).textValue();

        List<String> arrayPaths = getArrayPaths(transformer.getEntityType(), fullPath);

        if (arrayPaths.isEmpty()) {
            return node;
        }

        JsonNode resultNode = null;
        boolean duplicateProcessed = false;

        for (int i = arrayPaths.size() - 1; i >= 0; i--) {
            String input = (i > 0) ? "$$this.".concat(arrayPaths.get(i)) : "$".concat(arrayPaths.get(i));

            if (resultNode == null) {
                String nodeName = getNodeName(transformer.transform(source.getLeft()).asText(), arrayPaths, i);
                FilterNode left = new FieldNode(nodeName, source.getLeft().getPayload());

                left.setPayload(source.getLeft().getPayload());
                JsonNode newNode = transformer.getObjectMapper().createObjectNode().set("$and",
                        transformer.getObjectMapper().createArrayNode()
                                .add(transformer.getObjectMapper().createObjectNode()
                                        .set("$isArray", transformer.getObjectMapper().createArrayNode()
                                                .add(transformer.transform(source.getRight()))))
                                .add(transformer.getObjectMapper().createObjectNode().set("$in",
                                        transformer.getObjectMapper().createArrayNode()
                                                .add(transformer.transform(left))
                                                .add(transformer.transform(source.getRight())))));

                JsonNode ifNull = transformer.getObjectMapper().createObjectNode().set("$ifNull",
                        transformer.getObjectMapper().createArrayNode().add(input).add(transformer.getObjectMapper().createArrayNode()));

                resultNode = transformer.getObjectMapper().createObjectNode().set("$anyElementTrue",
                        transformer.getObjectMapper().createObjectNode().set("$map",
                                transformer.getObjectMapper().createObjectNode()
                                        .putPOJO("input", ifNull)
                                        .set("in", newNode)));

                String keyPath = fullPath.replace(nodeName.replace("$this", ""), "");
                duplicateProcessed = processDuplicates(
                        transformer,
                        keyPath,
                        resultNode,
                        fullPath
                );
            } else {
                JsonNode ifNull = transformer.getObjectMapper().createObjectNode().set("$ifNull",
                        transformer.getObjectMapper().createArrayNode().add(input).add(transformer.getObjectMapper().createArrayNode()));

                resultNode = transformer.getObjectMapper().createObjectNode().set("$anyElementTrue",
                        transformer.getObjectMapper().createObjectNode().set("$map",
                                transformer.getObjectMapper().createObjectNode()
                                        .putPOJO("input", ifNull)
                                        .set("in", resultNode)));

                String nodeName = getNodeName(transformer.transform(source.getLeft()).textValue(), arrayPaths, i);
                String keyPath = fullPath.replace(nodeName.replace("$this", ""), "");
                duplicateProcessed = processDuplicates(
                        transformer,
                        keyPath,
                        resultNode,
                        fullPath
                );
            }

        }
        return (duplicateProcessed) ? transformer.getObjectMapper().createObjectNode() : resultNode;
    }

    public JsonNode wrapArraysRegex(FilterJsonNodeTransformer transformer, JsonNode node, InfixOperationNode source) {
        if (!(source.getLeft() instanceof FieldNode)) {
            return node;
        }

        String fullPath = transformer.transform(source.getLeft()).textValue();

        List<String> arrayPaths = getArrayPaths(transformer.getEntityType(), fullPath);

        if (arrayPaths.isEmpty()) {
            return node;
        }

        JsonNode resultNode = null;
        boolean duplicateProcessed = false;

        for (int i = arrayPaths.size() - 1; i >= 0; i--) {
            String input = (i > 0) ? "$$this.".concat(arrayPaths.get(i)) : "$".concat(arrayPaths.get(i));

            if (resultNode == null) {
                JsonNode regex = node.findValue("$regexMatch");
                ObjectNode regexPayload = (ObjectNode) regex;

                String nodeName = getNodeNameRegex(transformer.transform(source.getLeft()).asText(), arrayPaths, i);
                regexPayload.put("input", nodeName);

                JsonNode ifNull = transformer.getObjectMapper().createObjectNode().set("$ifNull",
                        transformer.getObjectMapper().createArrayNode().add(input).add(transformer.getObjectMapper().createArrayNode()));

                resultNode = transformer.getObjectMapper().createObjectNode().set("$anyElementTrue",
                        transformer.getObjectMapper().createObjectNode().set("$map",
                                transformer.getObjectMapper().createObjectNode()
                                        .putPOJO("input", ifNull)
                                        .set("in", node)));

                String keyPath = fullPath.replace(nodeName.replace("$$this", ""), "");
                duplicateProcessed = processDuplicates(
                        transformer,
                        keyPath,
                        resultNode,
                        fullPath
                );
            } else {
                JsonNode ifNull = transformer.getObjectMapper().createObjectNode().set("$ifNull",
                        transformer.getObjectMapper().createArrayNode().add(input).add(transformer.getObjectMapper().createArrayNode()));

                resultNode = transformer.getObjectMapper().createObjectNode().set("$anyElementTrue",
                        transformer.getObjectMapper().createObjectNode().set("$map",
                                transformer.getObjectMapper().createObjectNode()
                                        .putPOJO("input", ifNull)
                                        .set("in", resultNode)));

                String nodeName = getNodeNameRegex(transformer.transform(source.getLeft()).textValue(), arrayPaths, i);
                String keyPath = fullPath.replace(nodeName.replace("$$this", ""), "");
                duplicateProcessed = processDuplicates(
                        transformer,
                        keyPath,
                        resultNode,
                        fullPath
                );
            }

        }
        return (duplicateProcessed) ? transformer.getObjectMapper().createObjectNode() : resultNode;
    }

    public JsonNode wrapArrays(FilterJsonNodeTransformer transformer, JsonNode node, InfixOperationNode source, String mongoOperator) {
        if (!(source.getLeft() instanceof FieldNode)) {
            return node;
        }

        String fullPath = transformer.transform(source.getLeft()).textValue();

        List<String> arrayPaths = getArrayPaths(transformer.getEntityType(), fullPath);

        if (arrayPaths.isEmpty()) {
            return node;
        }

        JsonNode resultNode = null;
        boolean duplicateProcessed = false;

        for (int i = arrayPaths.size() - 1; i >= 0; i--) {
            String input = (i > 0) ? "$$this.".concat(arrayPaths.get(i)) : "$".concat(arrayPaths.get(i));

            if (resultNode == null) {
                String nodeName = getNodeName(transformer.transform(source.getLeft()).asText(), arrayPaths, i);
                FilterNode left = new FieldNode(nodeName, source.getLeft().getPayload());
                left.setPayload(source.getLeft().getPayload());

                JsonNode newNode = transformer.getObjectMapper().createObjectNode().set(mongoOperator,
                        transformer.getObjectMapper().createArrayNode()
                                .add(transformer.transform(left))
                                .add(transformer.transform(source.getRight())));

                JsonNode ifNull = transformer.getObjectMapper().createObjectNode().set("$ifNull",
                        transformer.getObjectMapper().createArrayNode().add(input).add(transformer.getObjectMapper().createArrayNode()));

                resultNode = transformer.getObjectMapper().createObjectNode().set("$anyElementTrue",
                        transformer.getObjectMapper().createObjectNode().set("$map",
                                transformer.getObjectMapper().createObjectNode()
                                        .putPOJO("input", ifNull)
                                        .set("in", newNode)));

                String keyPath = fullPath.replace(nodeName.replace("$this", ""), "");
                duplicateProcessed = processDuplicates(
                        transformer,
                        keyPath,
                        resultNode,
                        fullPath
                );
            } else {
                JsonNode ifNull = transformer.getObjectMapper().createObjectNode().set("$ifNull",
                        transformer.getObjectMapper().createArrayNode().add(input).add(transformer.getObjectMapper().createArrayNode()));

                resultNode = transformer.getObjectMapper().createObjectNode().set("$anyElementTrue",
                        transformer.getObjectMapper().createObjectNode().set("$map",
                                transformer.getObjectMapper().createObjectNode()
                                        .putPOJO("input", ifNull)
                                        .set("in", resultNode)));

                String nodeName = getNodeName(transformer.transform(source.getLeft()).textValue(), arrayPaths, i);
                String keyPath = fullPath.replace(nodeName.replace("$this", ""), "");
                duplicateProcessed = processDuplicates(
                        transformer,
                        keyPath,
                        resultNode,
                        fullPath
                );
            }

        }

        return (duplicateProcessed) ? transformer.getObjectMapper().createObjectNode() : resultNode;
    }

    private List<String> getArrayPaths(Class<?> entityType, String textValue) {
        String[] fields = textValue.replace("$", "").split("\\.");

        List<String> arrayPaths = new ArrayList<>();

        String fullPath = "";
        String prevArrayPath = "";

        for (String field : fields) {

            if (fullPath.isEmpty()) {
                fullPath = fullPath.concat(field);
            } else {
                fullPath = fullPath.concat(".").concat(field);
            }

            Boolean isArray = fieldTypeResolver.resolve(entityType, fullPath.replace("_id", "id")).getIsArray();

            if (isArray) {
                String result = fullPath.replace(prevArrayPath, "");

                if (!arrayPaths.isEmpty()) result = result.replaceFirst("\\.", "");

                arrayPaths.add(result);
                prevArrayPath = fullPath;
            }
        }
        return arrayPaths;
    }

    private boolean processDuplicates(FilterJsonNodeTransformer transformer, String pathKey, JsonNode resultNode, String fullPath) {
        JsonNode cached = cachedNodes.get(pathKey);

        if (cached == null) {
            cachedNodes.put(pathKey, resultNode);
        } else {
            if (cachedPaths.contains(fullPath)) return true;

            List<JsonNode> cachedInNodes = cached.findValues("in");
            JsonNode cachedIn = cachedInNodes.get(cachedInNodes.size() - 1);

            List<JsonNode> resultInNodes = resultNode.findValues("in");
            JsonNode resultIn = resultInNodes.get(0);

            JsonNode andSearchResult = cachedIn.findValue("$and");
            JsonNode andResult;

            if (andSearchResult != null) {
                ((ArrayNode) andSearchResult).add(resultIn);
                andResult = andSearchResult;
            } else {
                andResult = transformer.getObjectMapper().createArrayNode()
                        .add(cachedIn.deepCopy())
                        .add(resultIn);
            }

            cachedIn = ((ObjectNode) cachedIn).removeAll();
            ((ObjectNode) cachedIn).set("$and", andResult);

            cachedPaths.add(fullPath);
            return true;
        }
        return false;
    }

    private String getNodeName(String name, List<String> arrayPaths, int index) {
        name = name.replace("$", "");

        List<String> split = Lists.newArrayList(name.split(arrayPaths.get(index), 2));

        if (split.size() == 1) {
            split.add("");
        }

        return "$this".concat(split.get(1));
    }

    private String getNodeNameRegex(String name, List<String> arrayPaths, int index) {
        return "$".concat(getNodeName(name, arrayPaths, index));
    }

    public void clearCache() {
        cachedNodes.clear();
        cachedPaths.clear();
    }
}
