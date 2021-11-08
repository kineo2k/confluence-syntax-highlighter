package com.yourdomain;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Scanned
public class HelloWorld implements Macro {

	private PageBuilderService pageBuilderService;

	@Autowired
	public HelloWorld(@ComponentImport PageBuilderService pageBuilderService) {
		this.pageBuilderService = pageBuilderService;
	}


	public String execute(Map<String, String> params, String bodyContents, ConversionContext conversionContext) throws MacroExecutionException {
		pageBuilderService.assembler().resources().requireWebResource("com.yourdomain.confluence-syntax-highlighter:confluence-syntax-highlighter");

		String  lang            = getValueAsString(params, "Language", "");
		String  title           = escapeHtml(getValueAsString(params, "Title", null));
		boolean collapse        = getValueAsBoolean(params, "Collapse");
		boolean lineNumber      = getValueAsBoolean(params, "LineNumber");
		int     startLineNumber = getValueAsInt(params, "StartLineNumber", 0);
		String  theme           = getValueAsString(params, "Theme", "atom-one-dark");
		String  code            = escapeHtml(bodyContents);

		String output = generateTheme(theme);

		output = output + generateTitleBlock(title, collapse);

		if (lineNumber) {
			output = output + generateLineNumberCodeBlock(lang, code, startLineNumber, collapse);
		} else {
			output = output + generateCodeBlock(lang, code, collapse);
		}

		return output;
	}

	@Override
	public BodyType getBodyType() {
		return BodyType.PLAIN_TEXT;
	}

	@Override
	public OutputType getOutputType() {
		return OutputType.BLOCK;
	}

	private String getValueAsString(Map<String, String> params, String key, String defaultValue) {
		if (params.containsKey(key)) {
			return params.get(key);
		}

		return defaultValue;
	}

	private boolean getValueAsBoolean(Map<String, String> params, String key) {
		if (params.containsKey(key)) {
			return Boolean.parseBoolean(params.get(key));
		}

		return false;
	}

	private int getValueAsInt(Map<String, String> params, String key, int defaultValue) {
		if (params.containsKey(key)) {
			try {
				return Integer.parseInt(params.get(key));
			} catch (Exception ex) {
				return defaultValue;
			}
		}

		return defaultValue;
	}

	private String escapeHtml(String value) {
		if (value == null) {
			return "";
		}

		return StringEscapeUtils.escapeHtml4(value);
	}

	private String generateTheme(String theme) {
		String style = "<style>";
		try (
				InputStream inputStream = this.getClass().getResourceAsStream("/css/" + theme + ".css");
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				Stream<String> lines = bufferedReader.lines();
		) {
			style = style + lines.collect(Collectors.joining());
		} catch (Exception ex) {}
		style = style + "</style>";

		return style;
	}

	private String generateTitleBlock(String title, boolean collapse) {
		if ("".equals(title) && !collapse) {
			return "";
		}

		StringBuilder builder = new StringBuilder()
				.append("<div class='sh-title-box'>")
				.append("<b>").append("".equals(title) ? "&nbsp;" : title).append("</b>");

		if (collapse) {
			builder.append("<span class='collapse-source expand-control sh-collapse' data-collapse='Y'>")
				   .append("<span class='expand-control-icon icon'>&nbsp;</span>")
				   .append("<span class='expand-control-text'>코드 블록 펼치기</span>")
				   .append("</span>");
		}

		builder.append("</div>");

		return builder.toString();
	}

	private String generateCodeBlock(String lang, String code, boolean collapse) {
		return new StringBuilder()
				.append("<pre class='sh-code-block").append(collapse ? " sh-hidden" : "").append("'>")
				.append("<code class='").append(lang).append("'>").append(code).append("</code>")
				.append("</pre>")
				.toString();
	}

	private String generateLineNumberCodeBlock(String lang, String code, int startLineNumber, boolean collapse) {
		return new StringBuilder()
				.append("<div class='sh-code-box").append(collapse ? " sh-hidden" : "").append("'>")
				.append("<table class='sh-line-number-code-block' border='0' cellpadding='0' cellspacing='0'>")
				.append("<tbody>")
				.append("<tr>")
				.append("<td class='sh-line-number-block hljs'>")
				.append(generateLineNumberBlock(code, startLineNumber))
				.append("</td>")
				.append("<td>")
				.append(generateCodeBlock(lang, code, false))
				.append("</td>")
				.append("</tr>")
				.append("</tbody>")
				.append("</table>")
				.append("</div>")
				.toString();
	}

	private String generateLineNumberBlock(String code, int startLineNumber) {
		StringBuilder builder = new StringBuilder();
		int           start   = startLineNumber == 0 ? 1 : startLineNumber;
		int           end     = numberOfLines(code) + (startLineNumber == 0 ? 0 : startLineNumber - 1);
		for (int i = start; i <= end; i++) {
			builder.append("<div class='sh-line-number-item hljs'>").append(i).append("</div>");
		}
		return builder.toString();
	}

	private int numberOfLines(String code) {
		String[] lines = code.split("\r\n|\r|\n");
		return lines.length;
	}
}
