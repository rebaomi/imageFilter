

import net.sf.json.JSONObject;
public class DefaultFont {

	private String fontSize;
	private String fontColor;
	private String fontFamily;
	private String fontPosition;
	
	public DefaultFont(JSONObject dfJson) {
		this.fontSize = dfJson.getString("fontSize");
		this.fontColor = dfJson.getString("fontColor");
		this.fontFamily = "'"+dfJson.getString("fontFamily")+"'";
		this.fontPosition = dfJson.getString("fontPosition");
		dfJson = null;
	}

	public String getFontSize() {
		return fontSize;
	}

	public void setFontSize(String fontSize) {
		this.fontSize = fontSize;
	}

	public String getFontColor() {
		return fontColor;
	}

	public void setFontColor(String fontColor) {
		this.fontColor = fontColor;
	}

	public String getFontFamily() {
		return fontFamily;
	}

	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
	}

	public String getFontPosition() {
		return fontPosition;
	}

	public void setFontPosition(String fontPosition) {
		this.fontPosition = fontPosition;
	}
	
	
	
}
