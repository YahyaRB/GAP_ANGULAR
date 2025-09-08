import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListeArticlesComponent } from './liste-articles.component';

describe('ListeArticlesComponent', () => {
  let component: ListeArticlesComponent;
  let fixture: ComponentFixture<ListeArticlesComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ListeArticlesComponent]
    });
    fixture = TestBed.createComponent(ListeArticlesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
