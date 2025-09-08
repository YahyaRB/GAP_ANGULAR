import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateAffectationComponent } from './update-affectation.component';

describe('UpdateAffectationComponent', () => {
  let component: UpdateAffectationComponent;
  let fixture: ComponentFixture<UpdateAffectationComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [UpdateAffectationComponent]
    });
    fixture = TestBed.createComponent(UpdateAffectationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
