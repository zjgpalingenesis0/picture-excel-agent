package com.zjg.pictureexcelagent.llm;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PromptTemplate {

    public String buildExtractionPrompt(String ocrText, String extractionRule, String fileName) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的数据提取专家。请从以下OCR识别的文本中提取结构化数据。\n\n");
        prompt.append("OCR识别文本:\n");
        prompt.append(ocrText);
        prompt.append("\n\n");

        // 添加文件名信息
        if (fileName != null && !fileName.isEmpty()) {
            prompt.append("原文件名: ").append(fileName).append("\n\n");
        }

        if (extractionRule != null && !extractionRule.isEmpty()) {
            prompt.append("提取规则:\n");
            prompt.append(extractionRule);
            prompt.append("\n\n");
        } else {
            prompt.append("提取规则:\n");
            prompt.append("1. 优先识别文本中的表格结构（表头、数据行）\n");
            prompt.append("2. 如果没有表格，尝试提取键值对信息（如：姓名=张三，年龄=25）\n");
            prompt.append("3. 如果没有键值对，提取所有独立的数据项（每行作为一个记录）\n");
            prompt.append("4. 保留所有重要的数字、日期、人名、金额等信息\n");
            prompt.append("5. 对于重复的水印或广告文字，请忽略\n");
            prompt.append("6. 尽量从文本中提取至少一条有用的记录\n\n");
        }

        prompt.append("请以JSON格式返回提取的数据，格式如下:\n");
        prompt.append("{\n");
        prompt.append("  \"dataType\": \"表格/键值对/文本行/文档\",\n");
        prompt.append("  \"records\": [\n");
        prompt.append("    {\"字段1\": \"值1\", \"字段2\": \"值2\", ...},\n");
        prompt.append("    {...}\n");
        prompt.append("  ],\n");
        prompt.append("  \"metadata\": {\n");
        prompt.append("    \"description\": \"数据来源和内容描述\",\n");
        prompt.append("    \"totalRecords\": 记录数量\n");
        prompt.append("  }\n");
        prompt.append("}\n\n");
        prompt.append("重要提示：\n");
        prompt.append("- 即使无法识别明确的表格结构，也要尽量提取有用的数据\n");
        prompt.append("- 如果是纯文本，可以将每行文本作为一个记录\n");
        prompt.append("- 如果是键值对，将每个键值对作为一个记录\n");
        prompt.append("- 请只返回JSON数据，不要包含任何解释性文字。");

        return prompt.toString();
    }

    public String buildValidationPrompt(String jsonData, String validationRules) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个数据验证专家。请验证以下JSON数据的完整性和正确性。\n\n");
        prompt.append("待验证数据:\n");
        prompt.append(jsonData);
        prompt.append("\n\n");

        if (validationRules != null && !validationRules.isEmpty()) {
            prompt.append("验证规则:\n");
            prompt.append(validationRules);
            prompt.append("\n\n");
        } else {
            prompt.append("验证规则:\n");
            prompt.append("1. 检查必填字段是否完整\n");
            prompt.append("2. 验证数据格式是否正确\n");
            prompt.append("3. 检查数据逻辑是否合理\n");
            prompt.append("4. 标识可疑或错误的数据\n\n");
        }

        prompt.append("请返回验证结果，格式如下:\n");
        prompt.append("{\n");
        prompt.append("  \"valid\": true/false,\n");
        prompt.append("  \"errors\": [\n");
        prompt.append("    {\"field\": \"字段名\", \"message\": \"错误描述\", \"value\": \"错误值\"}\n");
        prompt.append("  ],\n");
        prompt.append("  \"warnings\": [\n");
        prompt.append("    {\"field\": \"字段名\", \"message\": \"警告描述\"}\n");
        prompt.append("  ]\n");
        prompt.append("}\n\n");

        return prompt.toString();
    }

    public String buildCorrectionPrompt(String ocrText, String extractedData) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个数据纠错专家。请对比OCR原始文本和提取的数据，识别并纠正错误。\n\n");
        prompt.append("OCR原始文本:\n");
        prompt.append(ocrText);
        prompt.append("\n\n");
        prompt.append("提取的数据:\n");
        prompt.append(extractedData);
        prompt.append("\n\n");
        prompt.append("请:\n");
        prompt.append("1. 识别OCR识别错误的字符（如数字被识别为字母）\n");
        prompt.append("2. 纠正明显的拼写错误\n");
        prompt.append("3. 修复格式错误\n");
        prompt.append("4. 返回纠正后的数据\n\n");
        prompt.append("请返回纠正后的JSON数据。");

        return prompt.toString();
    }
}
