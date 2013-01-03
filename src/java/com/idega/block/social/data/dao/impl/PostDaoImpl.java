package com.idega.block.social.data.dao.impl;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import com.idega.block.social.data.PostEntity;
import com.idega.block.social.data.dao.PostDao;

@Repository(PostDao.BEAN_NAME)
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class PostDaoImpl extends PostDaoTemplateImpl<PostEntity> implements PostDao{
	@Override
	protected Class<PostEntity> getEntityClass(){
		return PostEntity.class;
	}
}