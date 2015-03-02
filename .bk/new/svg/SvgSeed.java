package com.eyuanku.web.framework.svg;

public interface SvgSeed {
	//取得svg图片的访问路径
	public String getSvgUrl(String imgKey);
	//取得img图片的访问路径
	public String getImgUrl(String imgKey);
	//取得img图片滤镜的访问路径
	public String getImgUrl(String imgKey, String effect);
	//取得空图片的访问路径
	public String getBlankImage();
}
