import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DuplicateAffectationComponent } from './duplicate-affectation.component';

describe('DuplicateAffectationComponent', () => {
  let component: DuplicateAffectationComponent;
  let fixture: ComponentFixture<DuplicateAffectationComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DuplicateAffectationComponent]
    });
    fixture = TestBed.createComponent(DuplicateAffectationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
