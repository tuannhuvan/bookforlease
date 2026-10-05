package org.example.demobookforlease.config;

import org.example.demobookforlease.model.*;
import org.example.demobookforlease.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final FinePolicyRepository finePolicyRepository;

    public DataInitializer(CategoryRepository categoryRepository,
                           BookRepository bookRepository,
                           MemberRepository memberRepository,
                           FinePolicyRepository finePolicyRepository) {
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.finePolicyRepository = finePolicyRepository;
    }

    @Override
    public void run(String... args) {
        if (finePolicyRepository.count() == 0) {
            FinePolicy defaultPolicy = new FinePolicy(
                    BigDecimal.valueOf(10000), // 10,000 VND / day
                    BigDecimal.valueOf(500000), // Max 500,000 VND
                    0, // 0 grace days
                    true
            );
            finePolicyRepository.save(defaultPolicy);
        }

        if (categoryRepository.count() == 0) {
            Category c1 = categoryRepository.save(new Category(null, "Lập trình & Công nghệ", "Sách học lập trình Java, Spring Boot, AI, Web", true));
            Category c2 = categoryRepository.save(new Category(null, "Kinh tế & Quản trị", "Sách kinh doanh, quản trị doanh nghiệp, tài chính", true));
            Category c3 = categoryRepository.save(new Category(null, "Văn học & Tiêu thuyết", "Tiểu thuyết, truyện ngắn, tác phẩm văn học", true));

            bookRepository.save(new Book(null, "Clean Code - Mã Sạch", "Robert C. Martin", "978-0132350884", c1, BigDecimal.valueOf(15000), BigDecimal.valueOf(250000), 10, 10, BookStatus.AVAILABLE));
            bookRepository.save(new Book(null, "Spring Boot in Action", "Craig Walls", "978-1617292545", c1, BigDecimal.valueOf(20000), BigDecimal.valueOf(300000), 5, 5, BookStatus.AVAILABLE));
            bookRepository.save(new Book(null, "Design Patterns", "Erich Gamma", "978-0201633610", c1, BigDecimal.valueOf(18000), BigDecimal.valueOf(280000), 7, 7, BookStatus.AVAILABLE));
            bookRepository.save(new Book(null, "Đắc Nhân Tâm", "Dale Carnegie", "978-0671027032", c2, BigDecimal.valueOf(10000), BigDecimal.valueOf(150000), 15, 15, BookStatus.AVAILABLE));
            bookRepository.save(new Book(null, "Nhà Giả Kim", "Paulo Coelho", "978-0061122415", c3, BigDecimal.valueOf(12000), BigDecimal.valueOf(180000), 12, 12, BookStatus.AVAILABLE));
        }

        if (memberRepository.count() == 0) {
            memberRepository.save(new Member(null, "Nguyễn Văn An", "an.nguyen@example.com", "0901234567", MemberStatus.ACTIVE));
            memberRepository.save(new Member(null, "Trần Thị Bình", "binh.tran@example.com", "0912345678", MemberStatus.ACTIVE));
            memberRepository.save(new Member(null, "Lê Hoàng Cường", "cuong.le@example.com", "0923456789", MemberStatus.ACTIVE));
        }
    }
}
