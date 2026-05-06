package com.spring.project.conf;

import com.spring.project.dto.BookItemDTO;
import com.spring.project.dto.OrderDTO;
import com.spring.project.model.BookItem;
import com.spring.project.model.Order;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.List;
import java.util.Locale;

@Configuration
public class BaseConfig implements WebMvcConfigurer {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        Converter<BookItem, BookItemDTO> bookItemToDto = ctx -> toBookItemDto(ctx.getSource());
        modelMapper.typeMap(BookItem.class, BookItemDTO.class).setConverter(bookItemToDto);

        Converter<Order, OrderDTO> orderToDto = ctx -> {
            Order src = ctx.getSource();
            if (src == null) {
                return null;
            }
            OrderDTO dto = new OrderDTO();
            dto.setId(src.getId());
            dto.setClientEmail(src.getClient() == null ? null : src.getClient().getEmail());
            dto.setEmployeeEmail(src.getEmployee() == null ? null : src.getEmployee().getEmail());
            dto.setOrderDate(src.getOrderDate());
            dto.setPrice(src.getPrice());
            dto.setStatus(src.getStatus());
            List<BookItem> items = src.getBookItems();
            dto.setBookItems(items == null ? List.of() : items.stream().map(BaseConfig::toBookItemDto).toList());
            return dto;
        };
        modelMapper.typeMap(Order.class, OrderDTO.class).setConverter(orderToDto);
        return modelMapper;
    }

    private static BookItemDTO toBookItemDto(BookItem src) {
        if (src == null) {
            return null;
        }
        BookItemDTO dto = new BookItemDTO();
        dto.setBookName(src.getBook() == null ? null : src.getBook().getName());
        dto.setQuantity(src.getQuantity());
        return dto;
    }

    @Bean
    @Primary
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames("messages");
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }

    @Bean
    @Primary
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("book-store-locale");
        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setCookiePath("/");
        resolver.setLanguageTagCompliant(true);
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
