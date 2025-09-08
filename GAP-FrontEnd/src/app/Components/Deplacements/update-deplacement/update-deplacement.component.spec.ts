import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateDeplacementComponent } from './update-deplacement.component';

describe('UpdateDeplacementComponent', () => {
  let component: UpdateDeplacementComponent;
  let fixture: ComponentFixture<UpdateDeplacementComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [UpdateDeplacementComponent]
    });
    fixture = TestBed.createComponent(UpdateDeplacementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
