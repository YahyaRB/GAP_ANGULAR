import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListeAffectationsComponent } from './liste-affectations.component';

describe('ListeAffectationsComponent', () => {
  let component: ListeAffectationsComponent;
  let fixture: ComponentFixture<ListeAffectationsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ListeAffectationsComponent]
    });
    fixture = TestBed.createComponent(ListeAffectationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
