package com.eyuanku.web.www.M;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.eyuanku.web.framework.M.BaseService;
import com.eyuanku.web.framework.M.SvgGenerate;
import com.eyuanku.web.framework.M.bean.Attach;
import com.eyuanku.web.framework.M.bean.Design;
import com.eyuanku.web.framework.M.bean.Material;
import com.eyuanku.web.framework.M.comm.BaseDesignService;
import com.eyuanku.web.framework.M.comm.ImageService;
import com.eyuanku.web.framework.dt.AuthUser;
import com.eyuanku.web.framework.exce.GlobalException;
import com.eyuanku.web.framework.storage.db.p.orm.ObjectPersistence;
import com.eyuanku.web.framework.storage.db.q.MapBean;
import com.eyuanku.web.framework.storage.db.q.Page;
import com.eyuanku.web.framework.storage.db.q.PageQuery;
import com.eyuanku.web.framework.storage.db.q.Query;
import com.eyuanku.web.framework.storage.file.IImgStorage;
import com.eyuanku.web.framework.storage.file.StorageBtype;
import com.eyuanku.web.framework.svg.handler.ImageCommonHandler;
import com.eyuanku.web.framework.svg.handler.ImageDownloadHandler;
import com.eyuanku.web.framework.svg.handler.SvgTranscoder;
import com.eyuanku.web.framework.svg.JsonDesign;
import com.eyuanku.web.framework.util.UploadUtil;
import com.eyuanku.web.www.bean.Share;
import com.eyuanku.web.www.bean.UserBean;

@Service("designService")
public class DesignService extends BaseDesignService {

	@Autowired
	private ImageService imageService;

	@Autowired
	private IImgStorage privateImageStorage;

	@Autowired
	private SvgGenerate svgGenerate;
	
	private static Map<Object, Object> EMAIL_STATE = new HashMap<Object, Object>();
	private static Map<Object, Object> CL_SHOW_OK = new HashMap<Object, Object>();
	private static Map<Object, Object> BUSINESS_TYPE = StorageBtype.getCodeList();

	static {
		CL_SHOW_OK.put(true, "显示");
		CL_SHOW_OK.put(false, "隐藏");

		EMAIL_STATE.put(0, "邮箱未验证");
		EMAIL_STATE.put(1, "邮箱已验证");
	}

	public List<MapBean> seachMaterialKind() {
		String sql = "select kind_id, kind_name, show_order"
				   + "     , date_format(create_time,'%Y-%c-%d %T'), create_user_id "
				   + "  from des_material_kind "
				   + " where show_ok = 1 "
				   + " order by show_order asc";
		Query query = new Query(masterSlaveDataSource);
		return query.query(sql);
	}

	// 根据素材分类取得素材
	public Page searchMaterialsByKind(Integer kindId, Integer pageNo,
			Integer pageSize) {
		String sql = "select d.material_id, d.kind_id, d.attach_id, a.business_type, d.content_type"
				   + "	   , a.img_key, a.img_ori_width, a.img_ori_height "
				   + "  from des_material d left join attach a on d.attach_id = a.attach_id"
				   + " where d.kind_id = ? "
				   + " and review_status = 2";
		Page page = new PageQuery(masterSlaveDataSource).page(pageNo, pageSize)
				.pageQuery(sql, new Object[] { kindId });
		BaseService.fillWithCodelist(page, BUSINESS_TYPE, "business_type");
		return page;
	}

	// 根据用户取得素材
	public Page searchMaterialsByOwner(Integer uid, Integer pageNo,
			Integer pageSize) {
		String sql = "select d.material_id, d.kind_id, d.attach_id, a.business_type, d.content_type "
				   + "	   , a.img_key, a.img_ori_width, a.img_ori_height "
				   + "  from des_material d left join attach a on d.attach_id = a.attach_id "
				   + " where d.create_user_id = ? "
				   + " and is_person = 1 "
				   + " order by material_id desc";
		Page page = new PageQuery(masterSlaveDataSource).page(pageNo, pageSize)
				.pageQuery(sql, new Object[] { uid });
		BaseService.fillWithCodelist(page, BUSINESS_TYPE, "business_type");
		return page;
	}

	// 根据word进行查询
	public Page searchMaterialsByWord(String word, Integer pageNo,
			Integer pageSize) {
		String sql = "select d.material_id, d.kind_id, d.attach_id, a.business_type, d.content_type "
				   + "	   , a.img_key, a.img_ori_width, a.img_ori_height "
				   + "  from des_material d left join attach a on d.attach_id = a.attach_id "
				   + " where is_person != 1 "
				   + " and keywords like ? ";
		Page page = new PageQuery(masterSlaveDataSource).page(pageNo, pageSize)
				.pageQuery(sql, new Object[] { "%" + word + "%" });
		BaseService.fillWithCodelist(page, BUSINESS_TYPE, "business_type");
		return page;
	}

	/**
	 * 上传个人素材
	 * 
	 * @param file
	 * @return
	 */
	public Material uploadMaterial(AuthUser authUser, MultipartFile file) {
		
		Attach attach = new Attach();
		attach.setBusinessType(StorageBtype.MATERIALS);
		attach.setCreateTime(new Date());
		attach.setCreateUserId(authUser.getUid());
		attach = imageService.saveImage(authUser, privateImageStorage, attach, file);

		InputStream is = null;
		try {
			is = file.getInputStream();
		} catch (IOException e) {
			throw new GlobalException("读取file的输入流失败");
		}
		String contentType = UploadUtil.getMultipartContentType(file);

		publicImageStorage.putImage(StorageBtype.MATERIALS, new Integer(
					attach.getAttachId()).toString(), is,
					contentType, false);

		Material material = new Material();
		material.setAttachId(attach.getAttachId());
		material.setContentType(contentType);
		material.setCreateTime(new Date());
		material.setHeight(Double.parseDouble(attach.getImgH()));
		material.setWidth(Double.parseDouble(attach.getImgW()));
		material.setIsPerson(1);
		material.setCreateUserId(authUser.getUid());
		ObjectPersistence objectPersistence = new ObjectPersistence(masterSlaveDataSource);
		int materialId = objectPersistence.insertObject(material);
		material.setMaterialId(materialId);
		return material;
	}

	// 保存或者更新设计json
	public Integer saveDesignJson(AuthUser authUser, Integer designKind, Integer designId, String json) {
		if (designId == null || designId == 0) {
			return saveDesign(authUser, designKind, json);
		}
		return updateDesign(designId, json) ? designId : 0;
	}
	
	/**
	 * 保存设计json
	 * 
	 * @param 设计类型
	 * @param json字符串
	 * @return 保存后的designId
	 */
	public Integer saveDesign(AuthUser authUser, Integer designKind, String json) {
		Attach attachBean = new Attach();
		attachBean.setBusinessType(StorageBtype.DESIGN);
		attachBean.setCreateUserId(authUser.getUid());
		attachBean.setCreateTime(new Date());

		Attach attach;
		byte[] jByte = null;
		try {
			jByte = json.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new GlobalException(e);
		}
		attach = attachService.saveAttach(authUser, privateFileStorage, attachBean,
				new ByteArrayInputStream(jByte));
		Design design = new Design();
		design.setContentAttachId(attach.getAttachId());
		design.setKindId(designKind);
		design.setIsRecommend(0);
		design.setCreateTime(new Date());
		design.setDesignTitle(JSONObject.fromObject(json).getString("title"));
		design.setUserId(authUser.getUid());

		ObjectPersistence op = new ObjectPersistence(masterSlaveDataSource);
		int designId = op.insertObject(design);
		design = null;
		return designId;
	}
	
	public Integer copyDesign2User(Integer designId, AuthUser authUser) {
		MapBean mb = findCavanWHByDesign(designId);
		Integer designKind = mb.getInt("kind_id");
		String designJson = findDesignJson(designId);
		return saveDesign(authUser, designKind, designJson);
	}

	// 根据imgkey获取svg字符串
	public String findSvgByKey(String svgKey) {
		InputStream is = publicFileStorage.getFile(StorageBtype.MATERIALS,
				svgKey).getContent();
		return ImageCommonHandler.InputStream2String(is);
	}
	
	public boolean updateDesign(Integer designId, String json) {
		String sql = "select design_title, content_attach_id"
				   + "     , business_type, file_key "
				   + "	from des_design left join attach on content_attach_id = attach_id "
				   + " where design_id = ?";
		Query query = new Query(masterSlaveDataSource);
		MapBean b = query.find(sql, new Object[] { designId });

		Attach attachBean = new Attach();
		attachBean.setAttachId(b.getInt("content_attach_id"));
		attachBean.setBusinessType(StorageBtype.DESIGN);
		attachBean.setKey(b.getString("file_key"));

		attachService.updateAttach(privateFileStorage, attachBean, json);

		Design design = new Design();
		design.setDesignId(designId);
		design.beginUpdate();
		design.setUpdateTime(new Date());
		
		design.setIsThumbnails(Thumbnails.UNMAKE.ordinal());
		String newTitle = JSONObject.fromObject(json).getString("title");
		if (!newTitle.equals(b.get("design_title"))) {
			design.setDesignTitle(newTitle);
		}

		ObjectPersistence op = new ObjectPersistence(masterSlaveDataSource);
		return op.updateObject(design) == 0 ? false : true;

	}

	public void makeDesign2PNG(Integer designId, String designTitle, HttpServletResponse response) {
		Map<String, InputStream> pngOSSMap = uploadDesignPNG(designId);
		boolean isSingle = pngOSSMap.size() <= 1 ? true : false;
		InputStream pngIs = ImageDownloadHandler
				.mergeImgStream(isSingle, pngOSSMap);
		pngOSSMap.clear();
		ImageDownloadHandler.injectImg2Response(designTitle,
				"image/png", pngIs, isSingle, response);
	}

	public void makeDesign2Pdf(boolean isHigh, Integer designId, String designTitle, HttpServletResponse response) {
		if (isHigh) {
			Map<String, InputStream> pdfOSSMap = uploadDesignPDF(designId);
			
			ImageDownloadHandler.generateHighPdf(pdfOSSMap, response,  designTitle);
			
			pdfOSSMap.clear();
		} else {
			List<OutputStream> pdfList = uploadDesignPNG4Pdf(designId);
			
			ImageDownloadHandler.generateCommonPdf(pdfList, response, designTitle);
			
			pdfList.clear();
		}
	}
	
	public Map<String, InputStream> uploadDesignPNG(Integer designId) {
		String desingJsonStr = findDesignJson(designId);
		JSONObject designJson = JSONObject.fromObject(desingJsonStr);
		JsonDesign jsonDesign = new JsonDesign(designJson);
		Map<Integer, InputStream> jsonDesignPageMap = JsonDesign.string2Stream(jsonDesign.toSvg(svgGenerate));
		
		Map<String, InputStream> pngOSSMap = new LinkedHashMap<String, InputStream>();
		ImageDownloadHandler.printCurTime("uploadDesignPNG开始处理图片");
		for (Map.Entry<Integer, InputStream> m : jsonDesignPageMap.entrySet()) {
			InputStream in = m.getValue();
			ByteArrayOutputStream out = null;
			InputStream is4Img = null;
			try {
				out = new ByteArrayOutputStream();
				SvgTranscoder.convert2PNG(in, out);

				is4Img = new ByteArrayInputStream(out.toByteArray());
				String pngName = m.getKey() + ".png";
				pngOSSMap.put(pngName, is4Img);
			} catch (JSONException e) {
				throw new GlobalException("json解析失败,请检查json格式是否正确", e);
			} catch (Exception e) {
				e.getCause().printStackTrace();
			}finally {
				try {
					if(out!=null){
						out.close();
					}
				} catch (IOException e) {
					throw new GlobalException(e);
				}
				try {
					if(is4Img!=null){
						is4Img.close();
					}
				} catch (IOException e) {
					throw new GlobalException(e);
				}
			}
		}
		ImageDownloadHandler.printCurTime("uploadDesignPNG处理图片结束");
		designJson = null;
		jsonDesign = null;
		jsonDesignPageMap.clear();
		return pngOSSMap;
	}
	
	public List<OutputStream> uploadDesignPNG4Pdf(Integer designId) {
		JSONObject designJson = JSONObject.fromObject(findDesignJson(designId));

		JsonDesign jsonDesign = new JsonDesign(designJson);
		Map<Integer, InputStream> pngMap = JsonDesign.string2Stream(jsonDesign.toSvg(svgGenerate));

		List<OutputStream> pngOSSMap = new ArrayList<OutputStream>();

		for (Map.Entry<Integer, InputStream> m : pngMap.entrySet()) {
			InputStream in = m.getValue();
			ByteArrayOutputStream out = null;
			try {
				out = new ByteArrayOutputStream();
				SvgTranscoder.convert2PNG(in, out);

				pngOSSMap.add(out);
			} catch (JSONException e) {
				throw new GlobalException("json解析失败,请检查json格式是否正确",
						e.getCause());
			} finally {
				try {
					out.close();
				} catch (IOException e) {
					throw new GlobalException(e);
				}
			}
		}
		designJson = null;
		jsonDesign = null;
		pngMap.clear();
		return pngOSSMap;
	}

	public Map<String, InputStream> uploadDesignPDF(Integer designId) {
		JSONObject designJson = JSONObject.fromObject(findDesignJson(designId));

		JsonDesign jsonDesign = new JsonDesign(designJson);
		Map<Integer, InputStream> pdfMap = JsonDesign.string2Stream(jsonDesign.toSvg(svgGenerate));

		Map<String, InputStream> pdfOSSMap = new LinkedHashMap<String, InputStream>();

		for (Map.Entry<Integer, InputStream> m : pdfMap.entrySet()) {
			InputStream in = m.getValue();

			ByteArrayOutputStream out = null;
			InputStream is4Pdf = null;
			try {
				out = new ByteArrayOutputStream();
				SvgTranscoder.convert2Pdf(in, out);

				is4Pdf = new ByteArrayInputStream(out.toByteArray());
				String pngName = m.getKey() + ".pdf";
				pdfOSSMap.put(pngName, is4Pdf);
			} catch (JSONException e) {
				throw new GlobalException("json解析失败,请检查json格式是否正确",
						e.getCause());
			} finally {
				try {
					out.close();
				} catch (IOException e) {
					throw new GlobalException(e);
				}
				try {
					is4Pdf.close();
				} catch (IOException e) {
					throw new GlobalException(e);
				}
			}
		}// end for loop
		designJson = null;
		jsonDesign = null;
		pdfMap.clear();
		return pdfOSSMap;
	}

	// 根据设计取得画布的宽和高
	public MapBean findCavanWHByDesign(int id) {
		StringBuffer sb = new StringBuffer();
		sb.append(      "select d.design_id, d.design_title, u.nickname, u.is_new_user, d.kind_id, d.user_id")
				.append("     , k.kind_title, a.img_key, k.kind_width, k.kind_height")
				.append("     , date_format(d.update_time,'%Y-%c-%d') as ft_last_update_time")
				.append("  from des_design as d")
				.append("  join org_user as u on d.user_id = u.user_id")
				.append("  join des_design_kind as k on d.kind_id = k.kind_id")
				.append("  join attach as a on d.content_attach_id = a.attach_id")
				.append(" where d.design_id = ? ");
		Query query = new Query(masterSlaveDataSource);
		MapBean template = query.find(sb.toString(), new Object[] { id });
		return template;
	}

	// 根据设计分类取得画布的宽和高
	public MapBean findCavanWHByKind(int kindId) {
		String sql = "select k.kind_id, k.kind_width, k.kind_height "
				   + "  from des_design_kind as k "
				   + " where kind_id= ? ";
		Query query = new Query(masterSlaveDataSource);
		MapBean mb = query.find(sql, new Object[] { kindId });
		return mb;
	}
	
	//添加到分享表数据
	public int insertShare(Share share) {
		ObjectPersistence objectPersistence = new ObjectPersistence(masterSlaveDataSource);
		return objectPersistence.insertObject(share);
	}
	
	//修改用户不是第一次登陆
	public int updateNewUser(UserBean ub) {
		ObjectPersistence objectPersistence = new ObjectPersistence(masterSlaveDataSource);
		return objectPersistence.updateObject(ub);
	}
}
