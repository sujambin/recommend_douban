package com.demo.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseBook<M extends BaseBook<M>> extends Model<M> implements IBean {

	public M setId(java.lang.Long id) {
		set("id", id);
		return (M)this;
	}
	
	public java.lang.Long getId() {
		return getLong("id");
	}

	public M setTitle(java.lang.String title) {
		set("title", title);
		return (M)this;
	}
	
	public java.lang.String getTitle() {
		return getStr("title");
	}

	public M setAuthor(java.lang.String author) {
		set("author", author);
		return (M)this;
	}
	
	public java.lang.String getAuthor() {
		return getStr("author");
	}

	public M setTranslator(java.lang.String translator) {
		set("translator", translator);
		return (M)this;
	}
	
	public java.lang.String getTranslator() {
		return getStr("translator");
	}

	public M setPubdate(java.lang.String pubdate) {
		set("pubdate", pubdate);
		return (M)this;
	}
	
	public java.lang.String getPubdate() {
		return getStr("pubdate");
	}

	public M setPrice(java.lang.String price) {
		set("price", price);
		return (M)this;
	}
	
	public java.lang.String getPrice() {
		return getStr("price");
	}

	public M setTags(java.lang.String tags) {
		set("tags", tags);
		return (M)this;
	}
	
	public java.lang.String getTags() {
		return getStr("tags");
	}

	public M setPublisher(java.lang.String publisher) {
		set("publisher", publisher);
		return (M)this;
	}
	
	public java.lang.String getPublisher() {
		return getStr("publisher");
	}

	public M setImage(java.lang.String image) {
		set("image", image);
		return (M)this;
	}
	
	public java.lang.String getImage() {
		return getStr("image");
	}

	public M setAuthorIntro(java.lang.String authorIntro) {
		set("author_intro", authorIntro);
		return (M)this;
	}
	
	public java.lang.String getAuthorIntro() {
		return getStr("author_intro");
	}

	public M setPages(java.lang.Integer pages) {
		set("pages", pages);
		return (M)this;
	}
	
	public java.lang.Integer getPages() {
		return getInt("pages");
	}

	public M setSummary(java.lang.String summary) {
		set("summary", summary);
		return (M)this;
	}
	
	public java.lang.String getSummary() {
		return getStr("summary");
	}

	public M setAlt(java.lang.String alt) {
		set("alt", alt);
		return (M)this;
	}
	
	public java.lang.String getAlt() {
		return getStr("alt");
	}

	public M setAltTitle(java.lang.String altTitle) {
		set("alt_title", altTitle);
		return (M)this;
	}
	
	public java.lang.String getAltTitle() {
		return getStr("alt_title");
	}

	public M setSeriesId(java.lang.Integer seriesId) {
		set("series_id", seriesId);
		return (M)this;
	}
	
	public java.lang.Integer getSeriesId() {
		return getInt("series_id");
	}

	public M setSeriesTitle(java.lang.String seriesTitle) {
		set("series_title", seriesTitle);
		return (M)this;
	}
	
	public java.lang.String getSeriesTitle() {
		return getStr("series_title");
	}

	public M setCatalog(java.lang.String catalog) {
		set("catalog", catalog);
		return (M)this;
	}
	
	public java.lang.String getCatalog() {
		return getStr("catalog");
	}

	public M setFlag(java.lang.Integer flag) {
		set("flag", flag);
		return (M)this;
	}
	
	public java.lang.Integer getFlag() {
		return getInt("flag");
	}

}