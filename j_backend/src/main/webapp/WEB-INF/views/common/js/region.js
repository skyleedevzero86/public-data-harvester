function filterByCity() {
  const cityFilter = document.getElementById('cityFilter');
  const districtFilter = document.getElementById('districtFilter');

  if (cityFilter && districtFilter) {
    const selectedCity = cityFilter.value;

    districtFilter.innerHTML = '<option value="">전체 구/군</option>';

    if (selectedCity) {
      const districts = getDistrictsForCity(selectedCity);
      districts.forEach(district => {
        const option = document.createElement('option');
        option.value = district;
        option.textContent = district;
        districtFilter.appendChild(option);
      });
    }

    applyFilters();
  }
}

function filterByDistrict() {
  applyFilters();
}

function clearFilters() {
  const cityFilter = document.getElementById('cityFilter');
  const districtFilter = document.getElementById('districtFilter');

  if (cityFilter) cityFilter.value = '';
  if (districtFilter) {
    districtFilter.value = '';
    districtFilter.innerHTML = '<option value="">전체 구/군</option>';
  }

  applyFilters();
}

function applyFilters() {
  const url = new URL(window.location);
  const cityFilter = document.getElementById('cityFilter');
  const districtFilter = document.getElementById('districtFilter');

  if (cityFilter && cityFilter.value) {
    url.searchParams.set('city', cityFilter.value);
  } else {
    url.searchParams.delete('city');
  }

  if (districtFilter && districtFilter.value) {
    url.searchParams.set('district', districtFilter.value);
  } else {
    url.searchParams.delete('district');
  }

  url.searchParams.delete('page');
  window.location.href = url.toString();
}

function getDistrictsForCity(city) {
  const districtsMap = {
    '서울특별시': [
      '강남구', '강동구', '강북구', '강서구', '관악구', '광진구', '구로구', '금천구',
      '노원구', '도봉구', '동대문구', '동작구', '마포구', '서대문구', '서초구', '성동구',
      '성북구', '송파구', '양천구', '영등포구', '용산구', '은평구', '종로구', '중구', '중랑구'
    ],
    '부산광역시': [
      '강서구', '금정구', '남구', '동구', '동래구', '부산진구', '북구', '사상구',
      '사하구', '서구', '수영구', '연제구', '영도구', '중구', '해운대구', '기장군'
    ],
    '대구광역시': [
      '남구', '달서구', '달성군', '동구', '북구', '서구', '수성구', '중구'
    ],
    '인천광역시': [
      '계양구', '남구', '남동구', '동구', '부평구', '서구', '연수구', '중구', '강화군', '옹진군'
    ],
    '광주광역시': ['광산구', '남구', '동구', '북구', '서구'],
    '대전광역시': ['대덕구', '동구', '서구', '유성구', '중구'],
    '울산광역시': ['남구', '동구', '북구', '울주군', '중구'],
    '경기도': [
      '가평군', '고양시', '과천시', '광명시', '광주시', '구리시', '군포시', '김포시',
      '남양주시', '동두천시', '부천시', '성남시', '수원시', '시흥시', '안산시', '안성시',
      '안양시', '양주시', '양평군', '여주시', '연천군', '오산시', '용인시', '의왕시',
      '의정부시', '이천시', '파주시', '평택시', '포천시', '하남시', '화성시'
    ],
    '강원특별자치도': [
      '강릉시', '고성군', '동해시', '삼척시', '속초시', '양구군', '양양군', '영월군',
      '원주시', '인제군', '정선군', '철원군', '춘천시', '태백시', '평창군', '홍천군',
      '화천군', '횡성군'
    ],
    '충청북도': [
      '괴산군', '단양군', '보은군', '영동군', '옥천군', '음성군', '제천시', '증평군',
      '진천군', '청주시', '충주시'
    ],
    '충청남도': [
      '공주시', '금산군', '논산시', '당진시', '보령시', '부여군', '서산시', '서천군',
      '아산시', '예산군', '천안시', '청양군', '태안군', '홍성군'
    ],
    '전라북도': [
      '고창군', '군산시', '김제시', '남원시', '무주군', '부안군', '순창군', '완주군',
      '익산시', '임실군', '장수군', '전주시', '정읍시', '진안군'
    ],
    '전라남도': [
      '강진군', '고흥군', '곡성군', '광양시', '구례군', '나주시', '담양군', '목포시',
      '무안군', '보성군', '순천시', '신안군', '여수시', '영광군', '영암군', '완도군',
      '장성군', '장흥군', '진도군', '함평군', '해남군', '화순군'
    ],
    '경상북도': [
      '경산시', '경주시', '고령군', '구미시', '군위군', '김천시', '문경시', '봉화군',
      '상주시', '성주군', '안동시', '영덕군', '영양군', '영주시', '영천시', '예천군',
      '울릉군', '울진군', '의성군', '청도군', '청송군', '칠곡군', '포항시'
    ],
    '경상남도': [
      '거제시', '거창군', '고성군', '김해시', '남해군', '밀양시', '사천시', '산청군',
      '양산시', '의령군', '진주시', '창녕군', '창원시', '통영시', '하동군', '함안군',
      '함양군', '합천군'
    ],
    '제주특별자치도': ['서귀포시', '제주시']
  };

  return districtsMap[city] || [];
}

function exportToExcel() {
  const url = new URL(window.location);
  url.searchParams.set('export', 'excel');
  window.open(url.toString(), '_blank');
}

function exportToCSV() {
  const url = new URL(window.location);
  url.searchParams.set('export', 'csv');
  window.open(url.toString(), '_blank');
}

function refreshData() {
  const refreshBtn = document.querySelector('.refresh-btn');
  if (refreshBtn) {
    refreshBtn.disabled = true;
    refreshBtn.innerHTML = '<i class="spinner-border spinner-border-sm me-2"></i>새로고침 중...';
  }

  window.location.reload();
}

function setupAutoRefresh(interval = 60000) {
  const autoRefreshCheckbox = document.getElementById('autoRefresh');
  let refreshInterval;

  if (autoRefreshCheckbox) {
    autoRefreshCheckbox.addEventListener('change', function() {
      if (this.checked) {
        refreshInterval = setInterval(() => {
          window.location.reload();
        }, interval);
      } else {
        clearInterval(refreshInterval);
      }
    });
  }
}

function setupSearch() {
  const searchInput = document.getElementById('regionSearch');
  if (searchInput) {
    let searchTimeout;
    searchInput.addEventListener('input', function() {
      clearTimeout(searchTimeout);
      searchTimeout = setTimeout(() => {
        performSearch(this.value);
      }, 500);
    });
  }
}

function performSearch(query) {
  const url = new URL(window.location);
  if (query.trim()) {
    url.searchParams.set('search', query.trim());
  } else {
    url.searchParams.delete('search');
  }
  url.searchParams.delete('page');
  window.location.href = url.toString();
}

function sortTable(column, direction = 'asc') {
  const url = new URL(window.location);
  url.searchParams.set('sort', column);
  url.searchParams.set('direction', direction);
  url.searchParams.delete('page');
  window.location.href = url.toString();
}

document.addEventListener('DOMContentLoaded', function() {
  const cityFilter = document.getElementById('cityFilter');
  const districtFilter = document.getElementById('districtFilter');

  if (cityFilter && cityFilter.value) {
    cityFilter.classList.add('filter-active');
    const districts = getDistrictsForCity(cityFilter.value);
    districts.forEach(district => {
      const option = document.createElement('option');
      option.value = district;
      option.textContent = district;
      if (district === districtFilter.value) {
        option.selected = true;
      }
      districtFilter.appendChild(option);
    });
  }

  if (districtFilter && districtFilter.value) {
    districtFilter.classList.add('filter-active');
  }

  setupAutoRefresh();
  setupSearch();

  const alerts = document.querySelectorAll('.alert-dismissible');
  alerts.forEach(alert => {
    setTimeout(() => {
      const bsAlert = new bootstrap.Alert(alert);
      bsAlert.close();
    }, 5000);
  });

  document.querySelectorAll("tbody tr").forEach((row) => {
    row.addEventListener("click", function (e) {
      if (
          e.target.closest("button") ||
          e.target.closest("a") ||
          e.target.closest("form")
      ) {
        return;
      }

      document
          .querySelectorAll("tbody tr")
          .forEach((r) => r.classList.remove("table-active"));

      this.classList.add("table-active");
    });
  });

  document.querySelectorAll('th[data-sort]').forEach(header => {
    header.style.cursor = 'pointer';
    header.addEventListener('click', function() {
      const column = this.dataset.sort;
      const currentSort = new URLSearchParams(window.location.search).get('sort');
      const currentDirection = new URLSearchParams(window.location.search).get('direction');

      let newDirection = 'asc';
      if (currentSort === column && currentDirection === 'asc') {
        newDirection = 'desc';
      }

      sortTable(column, newDirection);
    });
  });

  const exportExcelBtn = document.getElementById('exportExcel');
  if (exportExcelBtn) {
    exportExcelBtn.addEventListener('click', exportToExcel);
  }

  const exportCSVBtn = document.getElementById('exportCSV');
  if (exportCSVBtn) {
    exportCSVBtn.addEventListener('click', exportToCSV);
  }

  const refreshBtn = document.querySelector('.refresh-btn');
  if (refreshBtn) {
    refreshBtn.addEventListener('click', refreshData);
  }

  document.querySelectorAll('form').forEach(form => {
    form.addEventListener('submit', function() {
      const submitButton = this.querySelector('button[type="submit"]');
      if (submitButton) {
        submitButton.disabled = true;
        submitButton.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>처리 중...';
      }
    });
  });
});