import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddDeplacementComponent } from './add-deplacement.component';

describe('AddDeplacementComponent', () => {
  let component: AddDeplacementComponent;
  let fixture: ComponentFixture<AddDeplacementComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AddDeplacementComponent]
    });
    fixture = TestBed.createComponent(AddDeplacementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
