package it.eng.spagobi.behaviouralmodel.analyticaldriver.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIMetaModelParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.MetaModelParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.MetaModelParview;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiMetaModelParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParameters;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.tools.catalogue.metadata.SbiMetaModel;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class BIMetaModelDAOHibImpl extends AbstractHibernateDAO implements IBIMetaModelParameterDAO {
	static private Logger logger = Logger.getLogger(BIMetaModelDAOHibImpl.class);

	@Override
	public SbiMetaModelParameter loadById(Integer id) throws HibernateException {
		SbiMetaModelParameter metaModel = null;
		Session session = null;
		Transaction transaction = null;
		try {
			session = getSession();
			transaction = session.beginTransaction();
			metaModel = (SbiMetaModelParameter) session.load(SbiMetaModelParameter.class, id);
		} catch (HibernateException he) {
			logException(he);
			if (transaction != null)
				transaction.rollback();
			throw new SpagoBIRuntimeException(he.getLocalizedMessage(), he);
		} finally {
			if (session != null) {
				if (session.isOpen())
					session.close();
			}
		}
		return metaModel;
	}

	@Override
	public BIMetaModelParameter loadBIMetaModelParameterById(Integer id) {
		BIMetaModelParameter metaModel = null;
		Session session = null;
		Transaction transaction = null;
		try {
			session = getSession();
			transaction = session.beginTransaction();
			SbiMetaModelParameter sbiMetaModel = (SbiMetaModelParameter) session.load(SbiMetaModelParameter.class, id);
			if (sbiMetaModel != null)
				metaModel = toBIMetaModelParameter(sbiMetaModel);
			transaction.commit();
		} catch (HibernateException he) {
			logException(he);
			if (transaction != null)
				transaction.rollback();
			throw new SpagoBIRuntimeException(he.getMessage(), he);
		} finally {
			if (session != null) {
				if (session.isOpen())
					session.close();
			}
		}
		return metaModel;
	}

	@Override
	public void modifyBIMetaModelParameter(BIMetaModelParameter aBIMetaModelParameter) {
		Session session = null;
		Transaction transaction = null;
		try {
			session = getSession();
			transaction = session.beginTransaction();
			SbiMetaModelParameter hibBIMetaModelParameter = (SbiMetaModelParameter) session.load(SbiMetaModelParameter.class, aBIMetaModelParameter.getId());
			if (hibBIMetaModelParameter == null) {
				logger.error("The MetaModelParameter with id=" + aBIMetaModelParameter.getId() + " does not exist.");
			}

			SbiMetaModel hibMetaModel = (SbiMetaModel) session.load(SbiMetaModel.class, aBIMetaModelParameter.getBiMetaModelID());
			SbiParameters aSbiParameter = (SbiParameters) session.load(SbiParameters.class, aBIMetaModelParameter.getParID());

			hibBIMetaModelParameter.setSbiMetaModel(hibMetaModel);
			hibBIMetaModelParameter.setSbiParameter(aSbiParameter);
			hibBIMetaModelParameter.setLabel(aBIMetaModelParameter.getLabel());
			if (aBIMetaModelParameter.getRequired() != null)
				hibBIMetaModelParameter.setReqFl(new Short(aBIMetaModelParameter.getRequired().shortValue()));
			if (aBIMetaModelParameter.getModifiable() != null)
				hibBIMetaModelParameter.setModFl(new Short(aBIMetaModelParameter.getModifiable().shortValue()));
			if (aBIMetaModelParameter.getVisible() != null)
				hibBIMetaModelParameter.setViewFl(new Short(aBIMetaModelParameter.getVisible().shortValue()));
			if (aBIMetaModelParameter.getMultivalue() != null)
				hibBIMetaModelParameter.setMultFl(new Short(aBIMetaModelParameter.getMultivalue().shortValue()));
			hibBIMetaModelParameter.setParurlNm(aBIMetaModelParameter.getParameterUrlName());

			Integer colSpan = aBIMetaModelParameter.getColSpan();
			Integer thickPerc = aBIMetaModelParameter.getThickPerc();

			Integer oldPriority = hibBIMetaModelParameter.getPriority();
			Integer newPriority = aBIMetaModelParameter.getPriority();
			if (!oldPriority.equals(newPriority)) {
				Query query = null;
				if (oldPriority.intValue() > newPriority.intValue()) {
					String hqlUpdateShiftRight = "update SbiMetaModelParameter s set s.priority = (s.priority + 1) where s.priority >= " + newPriority
							+ " and s.priority < " + oldPriority + "and s.sbiMetaModel.id = " + hibMetaModel.getId();
					query = session.createQuery(hqlUpdateShiftRight);
				} else {
					String hqlUpdateShiftLeft = "update SbiMetaModelParameter s set s.priority = (s.priority - 1) where s.priority > " + oldPriority
							+ " and s.priority <= " + newPriority + "and s.sbiMetaModel.id = " + hibMetaModel.getId();
					query = session.createQuery(hqlUpdateShiftLeft);
				}
				query.executeUpdate();
			}
			hibBIMetaModelParameter.setPriority(newPriority);
			hibBIMetaModelParameter.setProg(new Integer(1));
			hibBIMetaModelParameter.setColSpan(colSpan);
			hibBIMetaModelParameter.setThickPerc(thickPerc);

			updateSbiCommonInfo4Update(hibBIMetaModelParameter);
			transaction.commit();
		} catch (HibernateException he) {
			logException(he);
			if (transaction != null)
				transaction.rollback();
			throw new HibernateException(he.getLocalizedMessage(), he);
		} finally {
			if (session != null) {
				if (session.isOpen())
					session.close();
			}
		}
	}

	@Override
	public Integer insertBIMetaModelParameter(BIMetaModelParameter aBIMetaModelParameter) {
		Session session = null;
		Transaction transaction = null;
		try {
			session = getSession();
			transaction = session.beginTransaction();

			SbiMetaModel hibMetaModel = (SbiMetaModel) session.load(SbiMetaModel.class, aBIMetaModelParameter.getBiMetaModelID());
			SbiParameters aSbiParameter = (SbiParameters) session.load(SbiParameters.class, aBIMetaModelParameter.getParID());

			SbiMetaModelParameter newHibMetaModelParameter = new SbiMetaModelParameter();

			newHibMetaModelParameter.setSbiMetaModel(hibMetaModel);
			newHibMetaModelParameter.setSbiParameter(aSbiParameter);
			newHibMetaModelParameter.setProg(new Integer(1));
			newHibMetaModelParameter.setLabel(aBIMetaModelParameter.getLabel());
			if (aBIMetaModelParameter.getRequired() != null)
				newHibMetaModelParameter.setReqFl(new Short(aBIMetaModelParameter.getRequired().shortValue()));
			if (aBIMetaModelParameter.getModifiable() != null)
				newHibMetaModelParameter.setModFl(new Short(aBIMetaModelParameter.getModifiable().shortValue()));
			if (aBIMetaModelParameter.getVisible() != null)
				newHibMetaModelParameter.setViewFl(new Short(aBIMetaModelParameter.getVisible().shortValue()));
			if (aBIMetaModelParameter.getMultivalue() != null)
				newHibMetaModelParameter.setMultFl(new Short(aBIMetaModelParameter.getMultivalue().shortValue()));

			newHibMetaModelParameter.setParurlNm(aBIMetaModelParameter.getParameterUrlName());
			newHibMetaModelParameter.setColSpan(aBIMetaModelParameter.getColSpan());
			newHibMetaModelParameter.setThickPerc(aBIMetaModelParameter.getThickPerc());

			String hqlUpdateShiftRight = "update SbiMetaModelParameter s set s.priority = (s.priority + 1) where s.priority >= "
					+ aBIMetaModelParameter.getPriority();// + " and s.sbiMetaModel.id = " + hibMetaModel.getId();
			Query query = session.createQuery(hqlUpdateShiftRight);
			query.executeUpdate();
			setTenant(getTenant());
			newHibMetaModelParameter.setPriority(aBIMetaModelParameter.getPriority());
			newHibMetaModelParameter.getCommonInfo().setOrganization(getTenant());
			updateSbiCommonInfo4Insert(newHibMetaModelParameter);
			Integer id = (Integer) session.save(newHibMetaModelParameter);

			transaction.commit();
			return id;
		} catch (HibernateException he) {
			logException(he);

			if (transaction != null)
				transaction.rollback();

			throw new SpagoBIRuntimeException(he.getLocalizedMessage(), he);

		} finally {

			if (session != null) {
				if (session.isOpen())
					session.close();
			}

		}

	}

	@Override
	public void eraseBIMetaModelParameter(BIMetaModelParameter aBIMetaModelParameter) {
		Session session = getSession();
		Transaction transaction = session.beginTransaction();

		SbiMetaModelParameter hibMetaModelParameter = (SbiMetaModelParameter) session.load(SbiMetaModelParameter.class, aBIMetaModelParameter.getId());

		if (hibMetaModelParameter == null) {
			logger.error("the BIMetaModelParameter with id=" + aBIMetaModelParameter.getId() + " does not exist.");
		}

		MetaModelParuseDAOHibImpl metaModelParuseDAO = new MetaModelParuseDAOHibImpl();
		List metaModelParuses = metaModelParuseDAO.loadAllParuses(hibMetaModelParameter.getMetaModelParId());
		Iterator itMetaModelParuses = metaModelParuses.iterator();
		while (itMetaModelParuses.hasNext()) {
			MetaModelParuse aMetaModelParuse = (MetaModelParuse) itMetaModelParuses.next();
			metaModelParuseDAO.eraseMetaModelParuse(aMetaModelParuse);
		}

		// deletes all MetaModelParuse object (dependencies) of the biMetaModelParameter that have a father relationship
		List metaModelParusesFather = metaModelParuseDAO.loadMetaModelParusesFather(hibMetaModelParameter.getMetaModelParId());
		Iterator itMetaModelParusesFather = metaModelParusesFather.iterator();
		while (itMetaModelParusesFather.hasNext()) {
			MetaModelParuse aMetaModelParuseFather = (MetaModelParuse) itMetaModelParusesFather.next();
			metaModelParuseDAO.eraseMetaModelParuse(aMetaModelParuseFather);
		}

		// delete also all MetaModelParView (visibility dependencies) of the biMetaModelParameter
		MetaModelParviewDAOHibImpl metaModelParviewDAO = new MetaModelParviewDAOHibImpl();
		List metaModelParview = metaModelParviewDAO.loadMetaModelParviews(hibMetaModelParameter.getMetaModelParId());
		Iterator itMetaModelParviews = metaModelParview.iterator();
		while (itMetaModelParviews.hasNext()) {
			MetaModelParview aMetaModelParview = (MetaModelParview) itMetaModelParviews.next();
			metaModelParviewDAO.eraseMetaModelParview(aMetaModelParview.getId());
		}

		// delete also all MetaModelParView (visibility dependencies) of the biMetaModelParameter father
		List metaModelParviewFather = metaModelParviewDAO.loadMetaModelParviewsFather(hibMetaModelParameter.getMetaModelParId());
		Iterator itMetaModelParviewsFather = metaModelParviewFather.iterator();
		while (itMetaModelParviewsFather.hasNext()) {
			MetaModelParview aMetaMOdelParviewFather = (MetaModelParview) itMetaModelParviewsFather.next();
			metaModelParviewDAO.eraseMetaModelParview(aMetaMOdelParviewFather.getId());
		}

		session.delete(hibMetaModelParameter);

		Integer metaModelId = hibMetaModelParameter.getSbiMetaModel().getId();

		String hqlUpdateShiftRight = "update SbiMetaModelParameter s set s.priority = (s.priority - 1) where s.priority >= "
				+ hibMetaModelParameter.getPriority() + " and s.sbiMetaModel.id = " + metaModelId;
		Query query = session.createQuery(hqlUpdateShiftRight);
		query.executeUpdate();
		transaction.commit();
	}

	@Override
	public List loadBIMetaModelParameterByMetaModelId(Integer metaModelID) {
		Session session = getSession();
		Transaction transaction = session.beginTransaction();
		List resultList = new ArrayList();
		try {
			String hql = "from SbiMetaModelParameter s where s.sbiMetaModel.id = " + metaModelID + " order by s.priority asc";

			Query hqlQuery = session.createQuery(hql);
			List hibMetaModelParameters = hqlQuery.list();

			Iterator it = hibMetaModelParameters.iterator();
			int count = 1;
			while (it.hasNext()) {
				BIMetaModelParameter metaModelParameter = toBIMetaModelParameter((SbiMetaModelParameter) it.next());
				// *****************************************************************
				// **************** START PRIORITY CONTROL *************************
				// *****************************************************************
				Integer priority = metaModelParameter.getPriority();
				// if the priority is different from the value expected,
				// recalculates it for all the parameter of the document
				if (priority == null || priority.intValue() != count) {
					logger.error(
							"The priorities of the biparameters for the document with id = " + metaModelID + " are not sorted. Priority recalculation starts.");
					recalculateBiParametersPriority(metaModelID, session);
					// restarts this method in order to load updated priorities
					metaModelParameter.setPriority(new Integer(count));
				}
				count++;
				// *****************************************************************
				// **************** END PRIORITY CONTROL ***************************
				// *****************************************************************
				resultList.add(metaModelParameter);
			}
			transaction.commit();
		} catch (HibernateException he) {
			logException(he);
			if (transaction != null)
				transaction.rollback();
			throw new SpagoBIRuntimeException(he.getLocalizedMessage(), he);
		} finally {
			if (session != null) {
				if (session.isOpen())
					session.close();
			}
		}
		return resultList;
	}

	@Override
	public void eraseBIMetaModelParametersByMetaModelId(Integer MetaModelId) {
		logger.debug("IN");
		Session session = null;
		SbiMetaModel hibMetaModel = null;
		Transaction transaction = null;
		try {
			session = getSession();
			transaction = session.beginTransaction();
			hibMetaModel = (SbiMetaModel) session.load(SbiMetaModel.class, MetaModelId);
			List<SbiMetaModelParameter> metaModelParameters = hibMetaModel.getSbiMetaModelParameters();

			logger.debug("delete all metaModelParameters for MetaModel with label " + hibMetaModel.getName());

			for (Iterator iterator = metaModelParameters.iterator(); iterator.hasNext();) {
				SbiMetaModelParameter HibMetaModelParameter = (SbiMetaModelParameter) iterator.next();
				BIMetaModelParameter biMetaModelParameter = toBIMetaModelParameter(HibMetaModelParameter);
				logger.debug("delete biMetaModelPar with label " + HibMetaModelParameter.getLabel() + " and url name " + HibMetaModelParameter.getParurlNm());
				eraseBIMetaModelParameter(biMetaModelParameter);
			}
		} catch (Exception he) {
			logger.error("Erro while deleting MetaModel pars associated to document with label = " + hibMetaModel != null ? hibMetaModel.getName() : "null",
					he);
			logException(he);
			if (transaction != null)
				transaction.rollback();
			throw new SpagoBIRuntimeException(he.getMessage(), he);
		} finally {
			if (session != null) {
				if (session.isOpen())
					session.close();
			}
		}
	}

	public void recalculateBiParametersPriority(Integer MetaModelId, Session aSession) {
		String hql = "from SbiMetaModelParameter s where s.sbiMetaModel.id = " + MetaModelId + " order by s.priority asc";
		Query hqlQuery = aSession.createQuery(hql);
		List hibMetaModelParameters = hqlQuery.list();
		Iterator it = hibMetaModelParameters.iterator();
		int count = 1;
		while (it.hasNext()) {
			SbiMetaModelParameter hibMetaModel = (SbiMetaModelParameter) it.next();
			hibMetaModel.setPriority(new Integer(count));
			count++;
			aSession.save(hibMetaModel);
		}
	}

	public BIMetaModelParameter toBIMetaModelParameter(SbiMetaModelParameter sbiMetaModelPar) {
		BIMetaModelParameter metaModel = new BIMetaModelParameter();
		metaModel.setId(sbiMetaModelPar.getMetaModelParId());
		metaModel.setLabel(sbiMetaModelPar.getLabel());
		if (sbiMetaModelPar.getModFl() != null)
			metaModel.setModifiable(new Integer(sbiMetaModelPar.getModFl().intValue()));
		if (sbiMetaModelPar.getMultFl() != null)
			metaModel.setMultivalue(new Integer(sbiMetaModelPar.getMultFl().intValue()));
		if (sbiMetaModelPar.getSbiMetaModel() != null)
			metaModel.setBiMetaModelID(sbiMetaModelPar.getSbiMetaModel().getId());
		if (sbiMetaModelPar.getParurlNm() != null)
			metaModel.setParameterUrlName(sbiMetaModelPar.getParurlNm());
		if (sbiMetaModelPar.getSbiParameter() != null)
			metaModel.setParID(sbiMetaModelPar.getSbiParameter().getParId());
		if (sbiMetaModelPar.getReqFl() != null)
			metaModel.setRequired(new Integer(sbiMetaModelPar.getReqFl().intValue()));
		if (sbiMetaModelPar.getViewFl() != null)
			metaModel.setVisible(new Integer(sbiMetaModelPar.getViewFl().intValue()));
		if (sbiMetaModelPar.getPriority() != null)
			metaModel.setPriority(sbiMetaModelPar.getPriority());
		if (sbiMetaModelPar.getProg() != null)
			metaModel.setProg(sbiMetaModelPar.getProg());
		if (sbiMetaModelPar.getColSpan() != null)
			metaModel.setColSpan(sbiMetaModelPar.getColSpan());
		if (sbiMetaModelPar.getThickPerc() != null)
			metaModel.setThickPerc(sbiMetaModelPar.getThickPerc());

		Parameter parameter = new Parameter();
		parameter.setId(sbiMetaModelPar.getSbiParameter().getParId());
		parameter.setType(sbiMetaModelPar.getSbiParameter().getParameterTypeCode());
		metaModel.setParameter(parameter);
		return metaModel;
	}

	@Override
	public SbiParameters getParameterByDriverName(String name) {

		String hqlQuery = "from SbiMetaModelParameter s where s.parurlNm =? ";
		Session session = null;
		Transaction transaction = null;

		try {
			session = getSession();
			transaction = session.beginTransaction();
			Query query = session.createQuery(hqlQuery);
			query.setString(0, name);
			SbiMetaModelParameter sbiMetaModelParam = (SbiMetaModelParameter) query.uniqueResult();

			Integer paramId = null;

			paramId = sbiMetaModelParam.getSbiParameter().getParId();

			String hQuery = "from SbiParameters sp where sp.parId = " + paramId;
			Query q = session.createQuery(hQuery);
			SbiParameters sbiParam = (SbiParameters) q.uniqueResult();

			return sbiParam;

		} catch (Exception e) {
			logger.error("Error getting parameters");
			throw new SpagoBIRuntimeException(e.getMessage(), e);
		} finally {
			if (session != null) {
				if (session.isOpen())
					session.close();
			}
		}

	}
}
