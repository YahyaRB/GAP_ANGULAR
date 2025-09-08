import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteAffectationComponent } from './delete-affectation.component';

describe('DeleteAffectationComponent', () => {
  let component: DeleteAffectationComponent;
  let fixture: ComponentFixture<DeleteAffectationComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DeleteAffectationComponent]
    });
    fixture = TestBed.createComponent(DeleteAffectationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
