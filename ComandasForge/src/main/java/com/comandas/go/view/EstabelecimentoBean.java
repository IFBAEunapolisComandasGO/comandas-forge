package com.comandas.go.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.comandas.go.model.Estabelecimento;
import java.util.List;

/**
 * Backing bean for Estabelecimento entities.
 * <p/>
 * This class provides CRUD functionality for all Estabelecimento entities. It
 * focuses purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt>
 * for state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD
 * framework or custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class EstabelecimentoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * Support creating and retrieving Estabelecimento entities
	 */

	private Long id;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	private Estabelecimento estabelecimento;

	public Estabelecimento getEstabelecimento() {
		return this.estabelecimento;
	}

	public void setEstabelecimento(Estabelecimento estabelecimento) {
		this.estabelecimento = estabelecimento;
	}

	@Inject
	private Conversation conversation;

	@PersistenceContext(unitName = "ComandasForge-persistence-unit", type = PersistenceContextType.EXTENDED)
	private EntityManager entityManager;

	public String create() {

		this.conversation.begin();
		this.conversation.setTimeout(1800000L);
		return "create?faces-redirect=true";
	}

	public void retrieve() {

		if (FacesContext.getCurrentInstance().isPostback()) {
			return;
		}

		if (this.conversation.isTransient()) {
			this.conversation.begin();
			this.conversation.setTimeout(1800000L);
		}

		if (this.id == null) {
			this.estabelecimento = this.example;
		} else {
			this.estabelecimento = findById(getId());
		}
	}

	public Estabelecimento findById(Long id) {

		return this.entityManager.find(Estabelecimento.class, id);
	}

	/*
	 * Support updating and deleting Estabelecimento entities
	 */

	public String update() {
		this.conversation.end();

		try {
			if (this.id == null) {
				this.entityManager.persist(this.estabelecimento);
				return "search?faces-redirect=true";
			} else {
				this.entityManager.merge(this.estabelecimento);
				return "view?faces-redirect=true&id="
						+ this.estabelecimento.getId();
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(e.getMessage()));
			return null;
		}
	}

	public String delete() {
		this.conversation.end();

		try {
			Estabelecimento deletableEntity = findById(getId());

			this.entityManager.remove(deletableEntity);
			this.entityManager.flush();
			return "search?faces-redirect=true";
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(e.getMessage()));
			return null;
		}
	}

	/*
	 * Support searching Estabelecimento entities with pagination
	 */

	private int page;
	private long count;
	private List<Estabelecimento> pageItems;

	private Estabelecimento example = new Estabelecimento();

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return 10;
	}

	public Estabelecimento getExample() {
		return this.example;
	}

	public void setExample(Estabelecimento example) {
		this.example = example;
	}

	public String search() {
		this.page = 0;
		return null;
	}

	public void paginate() {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

		// Populate this.count

		CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
		Root<Estabelecimento> root = countCriteria.from(Estabelecimento.class);
		countCriteria = countCriteria.select(builder.count(root)).where(
				getSearchPredicates(root));
		this.count = this.entityManager.createQuery(countCriteria)
				.getSingleResult();

		// Populate this.pageItems

		CriteriaQuery<Estabelecimento> criteria = builder
				.createQuery(Estabelecimento.class);
		root = criteria.from(Estabelecimento.class);
		TypedQuery<Estabelecimento> query = this.entityManager
				.createQuery(criteria.select(root).where(
						getSearchPredicates(root)));
		query.setFirstResult(this.page * getPageSize()).setMaxResults(
				getPageSize());
		this.pageItems = query.getResultList();
	}

	private Predicate[] getSearchPredicates(Root<Estabelecimento> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		String nome = this.example.getNome();
		if (nome != null && !"".equals(nome)) {
			predicatesList.add(builder.like(
					builder.lower(root.<String> get("nome")),
					'%' + nome.toLowerCase() + '%'));
		}
		List funcionarios = this.example.getFuncionarios();
		if (funcionarios != null) {
			predicatesList.add(builder.equal(root.get("funcionarios"),
					funcionarios));
		}
		List produtos = this.example.getProdutos();
		if (produtos != null) {
			predicatesList.add(builder.equal(root.get("produtos"), produtos));
		}

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<Estabelecimento> getPageItems() {
		return this.pageItems;
	}

	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back Estabelecimento entities (e.g. from
	 * inside an HtmlSelectOneMenu)
	 */

	public List<Estabelecimento> getAll() {

		CriteriaQuery<Estabelecimento> criteria = this.entityManager
				.getCriteriaBuilder().createQuery(Estabelecimento.class);
		return this.entityManager.createQuery(
				criteria.select(criteria.from(Estabelecimento.class)))
				.getResultList();
	}

	@Resource
	private SessionContext sessionContext;

	public Converter getConverter() {

		final EstabelecimentoBean ejbProxy = this.sessionContext
				.getBusinessObject(EstabelecimentoBean.class);

		return new Converter() {

			@Override
			public Object getAsObject(FacesContext context,
					UIComponent component, String value) {

				return ejbProxy.findById(Long.valueOf(value));
			}

			@Override
			public String getAsString(FacesContext context,
					UIComponent component, Object value) {

				if (value == null) {
					return "";
				}

				return String.valueOf(((Estabelecimento) value).getId());
			}
		};
	}

	/*
	 * Support adding children to bidirectional, one-to-many tables
	 */

	private Estabelecimento add = new Estabelecimento();

	public Estabelecimento getAdd() {
		return this.add;
	}

	public Estabelecimento getAdded() {
		Estabelecimento added = this.add;
		this.add = new Estabelecimento();
		return added;
	}
}
