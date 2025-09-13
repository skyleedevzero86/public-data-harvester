package com.antock.global.utils;

import com.antock.global.common.constants.PaginationConstants;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtil {

    public static Pageable createPageable(int page, int size, String sort) {

        page = Math.max(PaginationConstants.MIN_PAGE_NUMBER, page);

        size = Math.min(PaginationConstants.MAX_PAGE_SIZE,
                Math.max(PaginationConstants.MIN_PAGE_SIZE, size));

        Sort sortObj = createSort(sort);

        return PageRequest.of(page, size, sortObj);
    }

    public static Sort createSort(String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            return Sort.by(Sort.Direction.DESC, PaginationConstants.DEFAULT_SORT_FIELD);
        }

        String[] sortParts = sort.split(PaginationConstants.SORT_DIRECTION_SEPARATOR);
        if (sortParts.length != 2) {
            return Sort.by(Sort.Direction.DESC, PaginationConstants.DEFAULT_SORT_FIELD);
        }

        String field = sortParts[0].trim();
        String direction = sortParts[1].trim();

        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(sortDirection, field);
    }

    public static boolean isValidPagination(int page, int size) {
        return page >= PaginationConstants.MIN_PAGE_NUMBER
                && size >= PaginationConstants.MIN_PAGE_SIZE
                && size <= PaginationConstants.MAX_PAGE_SIZE;
    }

    public static int calculateTotalPages(long totalElements, int pageSize) {
        return (int) Math.ceil((double) totalElements / pageSize);
    }

    public static boolean hasNextPage(int currentPage, int totalPages) {
        return (currentPage + 1) < totalPages;
    }

    public static boolean hasPreviousPage(int currentPage) {
        return currentPage > 0;
    }
}