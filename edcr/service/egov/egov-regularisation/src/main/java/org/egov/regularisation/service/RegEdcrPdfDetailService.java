package org.egov.regularisation.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.egov.regularisation.repository.RegEdcrPdfDetailRepository;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RegEdcrPdfDetailService {

    @Autowired
    private RegEdcrPdfDetailRepository edcrPdfDetailRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    public void save(org.egov.regularisation.entity.RegEdcrPdfDetail regEdcrPdfDetail) {
        edcrPdfDetailRepository.save(regEdcrPdfDetail);
    }

    public void saveAll(List<org.egov.regularisation.entity.RegEdcrPdfDetail> regEdcrPdfDetails) {
        edcrPdfDetailRepository.save(regEdcrPdfDetails);
    }

//    public List<org.egov.regularisation.entity.RegEdcrPdfDetail> findByDcrApplicationId(Long applicationDetailId) {
//        return edcrPdfDetailRepository.findByEdcrApplicationDetailId(applicationDetailId);
//    }


}
